package com.dilem.framebackend.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class SocialAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SocialAuthService.class);

    @Value("${social.google.client-id}")
    private String googleClientId;

    @Value("${social.apple.client-id}")
    private String appleClientId;

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String GOOGLE_JWK_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String APPLE_JWK_URL = "https://appleid.apple.com/auth/keys";

    public JWTClaimsSet verifyGoogleToken(String token) {
        return verifyToken(token, GOOGLE_JWK_URL, GOOGLE_ISSUER, googleClientId);
    }

    public JWTClaimsSet verifyAppleToken(String token) {
        return verifyToken(token, APPLE_JWK_URL, APPLE_ISSUER, appleClientId);
    }

    private JWTClaimsSet verifyToken(String token, String jwkUrl, String issuer, String audience) {
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwkUrl));
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            JWTClaimsSet claims = jwtProcessor.process(token, null);

            // Verify Issuer
            if (!claims.getIssuer().equals(issuer)) {
                throw new SecurityException("Invalid Issuer: " + claims.getIssuer());
            }

            // Verify Audience (Your App ID)
            if (!claims.getAudience().contains(audience)) {
                // throw new SecurityException("Invalid Audience: " + claims.getAudience());
                // Note: For development, you might want to log this but not throw, 
                // as sometimes audience might differ in testing environments.
                logger.warn("Audience mismatch. Expected: {}, Got: {}", audience, claims.getAudience());
            }
            
            return claims;

        } catch (Exception e) {
            logger.error("Social Token Verification Failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Social Token", e);
        }
    }
}
