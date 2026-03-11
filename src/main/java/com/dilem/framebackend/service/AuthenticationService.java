package com.dilem.framebackend.service;

import com.dilem.framebackend.model.RefreshToken;
import com.dilem.framebackend.model.User;
import com.dilem.framebackend.model.dto.UserDto;
import com.dilem.framebackend.model.dto.auth.AuthenticationRequest;
import com.dilem.framebackend.model.dto.auth.AuthenticationResponse;
import com.dilem.framebackend.model.dto.auth.RegisterRequest;
import com.dilem.framebackend.repository.RefreshTokenRepository;
import com.dilem.framebackend.repository.UserRepository;
import com.dilem.framebackend.util.JwtUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RefreshTokenService refreshTokenService, SocialAuthService socialAuthService, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.socialAuthService = socialAuthService;
        this.auditService = auditService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
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
        return processSocialLogin(claims, "Google");
    }

    @Transactional
    public AuthenticationResponse loginWithApple(String idToken) {
        JWTClaimsSet claims = socialAuthService.verifyAppleToken(idToken);
        return processSocialLogin(claims, "Apple");
    }

    private AuthenticationResponse processSocialLogin(JWTClaimsSet claims, String provider) {
        String email = (String) claims.getClaim("email");
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in " + provider + " token");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstname((String) claims.getClaim("given_name"));
            newUser.setLastname((String) claims.getClaim("family_name"));
            
            if (newUser.getFirstname() == null) newUser.setFirstname(provider);
            if (newUser.getLastname() == null) newUser.setLastname("User");
            
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); 
            
            return userRepository.save(newUser);
        });

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));
        
        auditService.logLoginSuccess(email, provider);

        return AuthenticationResponse.of(jwt, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
    }
}
