package com.dilem.framebackend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> ipCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> emailCache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return ipCache.computeIfAbsent(key, this::newBucket);
    }
    
    public Bucket resolveEmailBucket(String email) {
        return emailCache.computeIfAbsent(email, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // Allow 5 login attempts per minute per IP or Email
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
