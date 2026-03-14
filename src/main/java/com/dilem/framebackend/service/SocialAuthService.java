package com.dilem.framebackend.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class SocialAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SocialAuthService.class);

    @Value("${social.google.client-id}")
    private String googleClientId;

    @Value("${social.apple.client-id}")
    private String appleClientId;
    
    @Value("${social.apple.client-secret:PLACEHOLDER}")
    private String appleClientSecret;

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String GOOGLE_JWK_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String APPLE_JWK_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    public JWTClaimsSet verifyGoogleToken(String token) {
        return verifyToken(token, GOOGLE_JWK_URL, GOOGLE_ISSUER, googleClientId);
    }

    public JWTClaimsSet verifyAppleToken(String token) {
        return verifyToken(token, APPLE_JWK_URL, APPLE_ISSUER, appleClientId);
    }
    
    public void revokeAppleToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) return;
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", appleClientId);
            map.add("client_secret", appleClientSecret);
            map.add("token", accessToken);
            map.add("token_type_hint", "access_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            
            restTemplate.postForObject(APPLE_REVOKE_URL, request, String.class);
            logger.info("Apple token revoked successfully.");
        } catch (Exception e) {
            logger.error("Failed to revoke Apple token: {}", e.getMessage());
        }
    }

    private JWTClaimsSet verifyToken(String token, String jwkUrl, String issuer, String audience) {
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            
            // Fix 1: Use URI.create().toURL() to avoid Java 20 'new URL(String)' deprecation
            // Fix 2: Provide explicit ResourceRetriever to avoid RemoteJWKSet single-arg deprecation
            ResourceRetriever resourceRetriever = new DefaultResourceRetriever(5000, 5000);
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(URI.create(jwkUrl).toURL(), resourceRetriever);
            
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            JWTClaimsSet claims = jwtProcessor.process(token, null);

            if (!claims.getIssuer().equals(issuer)) {
                throw new SecurityException("Invalid Issuer: " + claims.getIssuer());
            }

            if (!claims.getAudience().contains(audience)) {
                throw new SecurityException("Invalid Audience: " + claims.getAudience());
            }
            
            return claims;

        } catch (Exception e) {
            logger.error("Social Token Verification Failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Social Token", e);
        }
    }
}
