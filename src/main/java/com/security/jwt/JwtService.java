package com.security.jwt;


import com.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = buildSigningKey(jwtConfig.getSecret());
    }

    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: jwt.secret");
        }

        // Prefer Base64 secrets (recommended) but avoid accidentally decoding plain-text secrets.
        // To use Base64, prefix the config with: base64:<value>
        byte[] keyBytes;
        if (secret.regionMatches(true, 0, "base64:", 0, "base64:".length())) {
            String b64 = secret.substring("base64:".length()).trim();
            keyBytes = Decoders.BASE64.decode(b64);
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        try {
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException ex) {
            throw new IllegalStateException(
                    "JWT secret is too weak for HS256. Use at least 32 bytes (256 bits), preferably a Base64-encoded value.",
                    ex
            );
        }
    }

    public String generateToken(String username){
        long expirationMs = jwtConfig.getAccessTokenExpiration();
        if (expirationMs <= 0) {
            // Keep a safe default rather than silently generating non-expiring tokens.
            expirationMs = 1000L * 60 * 60;
        }

        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

    }

    public String extractUsername(String token){
        return getClaimsJws(token).getPayload().getSubject();
    }

    private Jws<Claims> getClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean validateUsernameAndToken(String username,
                                            UserDetails userDetails,
                                            String token) {

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaimsJws(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
