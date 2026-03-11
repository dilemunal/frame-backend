package com.dilem.framebackend.service;

import com.dilem.framebackend.repository.RefreshTokenRepository;
import com.dilem.framebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void deleteUserAccount(Long userId) {
        // 1. Delete Refresh Tokens
        refreshTokenRepository.deleteByUser(userRepository.findById(userId.intValue()).orElseThrow());

        // TODO: Delete all user-related data (Journal Entries, Media Assets, etc.)
        // This is a critical step for GDPR/KVKK compliance.

        // 3. Finally, Delete the User
        userRepository.deleteById(userId.intValue());
        
        // 4. Audit Log
        auditService.logAccountDeletion(userId);
    }
}
