package com.fitness.notificationservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Semneaza un JWT HMAC (HS256) cu identitatea acestui serviciu, pentru apelurile inter-servicii.
 * Secretul este partajat prin config-server (internal.jwt.secret).
 */
@Component
public class InternalJwtService {

    private final SecretKey key;
    private final String issuer;
    private final long ttlSeconds;
    private final String serviceName;

    public InternalJwtService(
            @Value("${internal.jwt.secret}") String secret,
            @Value("${internal.jwt.issuer}") String issuer,
            @Value("${internal.jwt.ttl-seconds}") long ttlSeconds,
            @Value("${spring.application.name}") String serviceName) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
        this.serviceName = serviceName;
    }

    public String generateToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(serviceName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }
}
