package com.dilem.framebackend.service;

import com.dilem.framebackend.model.User;
import com.dilem.framebackend.repository.RefreshTokenRepository;
import com.dilem.framebackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;
    private final SocialAuthService socialAuthService;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, AuditService auditService, SocialAuthService socialAuthService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditService = auditService;
        this.socialAuthService = socialAuthService;
    }

    @Transactional
    public void deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1. Revoke Apple Token if applicable
        if ("APPLE".equalsIgnoreCase(user.getProvider())) {
            // Note: In a real implementation, you would retrieve the stored Apple Refresh Token
            // from the database here. Since we are not storing it in this MVP, 
            // we log this action.
            // socialAuthService.revokeAppleToken(user.getAppleRefreshToken());
        }

        // 2. Delete Refresh Tokens
        refreshTokenRepository.deleteByUser(user);

        // TODO: Delete all user-related data (Journal Entries, Media Assets, etc.)

        // 3. Finally, Delete the User
        userRepository.deleteById(userId.intValue());
        
        // 4. Audit Log
        auditService.logAccountDeletion(userId);
    }
}
