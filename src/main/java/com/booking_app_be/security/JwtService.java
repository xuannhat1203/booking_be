package com.booking_app_be.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    // TODO: chuyển sang cấu hình (application.yml) trong môi trường thực tế
    private static final String SECRET_KEY = "change-this-secret-key-to-a-longer-secure-value";

    private byte[] getSigningKeyBytes() {
        return SECRET_KEY.getBytes(StandardCharsets.UTF_8);
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKeyBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(24, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, getSigningKeyBytes())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(getSigningKeyBytes())
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
