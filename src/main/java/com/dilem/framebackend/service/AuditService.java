package com.dilem.framebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public void logLoginSuccess(String email, String ipAddress) {
        logger.info("AUDIT: Login Success | User: {} | IP: {}", email, ipAddress);
    }

    public void logLoginFailure(String email, String ipAddress, String reason) {
        logger.warn("AUDIT: Login Failure | User: {} | IP: {} | Reason: {}", email, ipAddress, reason);
    }

    public void logAccountDeletion(Long userId) {
        logger.warn("AUDIT: Account Deleted | UserId: {}", userId);
    }
}
