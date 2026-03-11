package com.dilem.framebackend.service;

import com.dilem.framebackend.model.RefreshToken;
import com.dilem.framebackend.model.User;
import com.dilem.framebackend.model.dto.UserDto;
import com.dilem.framebackend.model.dto.auth.AuthenticationRequest;
import com.dilem.framebackend.model.dto.auth.AuthenticationResponse;
import com.dilem.framebackend.model.dto.auth.RegisterRequest;
import com.dilem.framebackend.model.enums.AuthProvider;
import com.dilem.framebackend.repository.RefreshTokenRepository;
import com.dilem.framebackend.repository.UserRepository;
import com.dilem.framebackend.util.JwtUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    @Value("${application.security.jwt.access-token.expiration}")
    private long jwtExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthService socialAuthService;
    private final AuditService auditService;
    private final RateLimitingService rateLimitingService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RefreshTokenService refreshTokenService, SocialAuthService socialAuthService, AuditService auditService, RateLimitingService rateLimitingService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.socialAuthService = socialAuthService;
        this.auditService = auditService;
        this.rateLimitingService = rateLimitingService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Bucket bucket = rateLimitingService.resolveEmailBucket(request.email());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts for this email.");
        }

        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (user.getProvider() != AuthProvider.LOCAL) {
                throw new BadCredentialsException("Please use your social account to login.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User userDetails = (User) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(authentication);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(userDetails.getId()));
            
            auditService.logLoginSuccess(request.email(), "API");

            return AuthenticationResponse.of(jwt, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(userDetails));
        } catch (Exception e) {
            auditService.logLoginFailure(request.email(), "API", e.getMessage());
            throw e;
        }
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = new User();
        user.setFirstname(request.firstName());
        user.setLastname(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProvider(AuthProvider.LOCAL);

        userRepository.save(user);

        return authenticate(new AuthenticationRequest(request.email(), request.password()));
    }

    @Transactional
    public AuthenticationResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = token.getUser();
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
                    
                    refreshTokenRepository.delete(token);
                    
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));
                    
                    return AuthenticationResponse.of(newAccessToken, newRefreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
                })
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));
    }

    @Transactional
    public AuthenticationResponse loginWithGoogle(String idToken) {
        JWTClaimsSet claims = socialAuthService.verifyGoogleToken(idToken);
        return processSocialLogin(claims, AuthProvider.GOOGLE);
    }

    @Transactional
    public AuthenticationResponse loginWithApple(String idToken) {
        JWTClaimsSet claims = socialAuthService.verifyAppleToken(idToken);
        return processSocialLogin(claims, AuthProvider.APPLE);
    }

    private AuthenticationResponse processSocialLogin(JWTClaimsSet claims, AuthProvider provider) {
        String email = (String) claims.getClaim("email");
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in " + provider + " token");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstname((String) claims.getClaim("given_name"));
            newUser.setLastname((String) claims.getClaim("family_name"));
            newUser.setProvider(provider);
            newUser.setPassword(null); // Social users don't have a password
            
            if (newUser.getFirstname() == null) newUser.setFirstname(provider.toString());
            if (newUser.getLastname() == null) newUser.setLastname("User");
            
            return userRepository.save(newUser);
        });

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));
        
        auditService.logLoginSuccess(email, provider.toString());

        return AuthenticationResponse.of(jwt, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
    }
}
