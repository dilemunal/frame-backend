package com.dilem.framebackend.service;

import com.dilem.framebackend.model.RefreshToken;
import com.dilem.framebackend.model.User;
import com.dilem.framebackend.model.dto.UserDto;
import com.dilem.framebackend.model.dto.auth.AuthenticationRequest;
import com.dilem.framebackend.model.dto.auth.AuthenticationResponse;
import com.dilem.framebackend.model.dto.auth.RegisterRequest;
import com.dilem.framebackend.repository.UserRepository;
import com.dilem.framebackend.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    @Value("${application.security.jwt.access-token.expiration}")
    private long jwtExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthService socialAuthService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RefreshTokenService refreshTokenService, SocialAuthService socialAuthService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.socialAuthService = socialAuthService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userDetails = (User) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(userDetails.getId()));

        return AuthenticationResponse.of(jwt, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(userDetails));
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

        // Auto-login after registration
        return authenticate(new AuthenticationRequest(request.email(), request.password()));
    }

    @Transactional
    public AuthenticationResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    
                    // Rotation: Delete old refresh token, create new one
                    refreshTokenService.deleteByUserId(Long.valueOf(user.getId()));
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));
                    
                    return AuthenticationResponse.of(token, newRefreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
                })
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));
    }
}
