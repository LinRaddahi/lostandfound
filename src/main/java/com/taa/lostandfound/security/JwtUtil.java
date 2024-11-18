package com.taa.lostandfound.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taa.lostandfound.error.JwtException;
import com.taa.lostandfound.error.JwtValidationException;
import com.taa.lostandfound.model.RoleDTO;
import com.taa.lostandfound.model.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@EnableConfigurationProperties(SecurityConfigProperties.class)
public class JwtUtil {
    private final String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JwtUtil(SecurityConfigProperties securityConfigProperties) {
        this.secretKey = securityConfigProperties.secret();
    }

    public String generateToken(String userId, String name, List<String> roles) {
        log.info("Generating token for user: {}", userId);
        try {
            return Jwts.builder()
                    .subject(userId)
                    .claim("name", name)
                    .claim("roles", roles)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
            throw new JwtException("Error generating token", e);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        log.info("Validating token for user: {}", userDetails.getUsername());
        try {
            String userId = extractUserId(token);
            return (userId.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            throw new JwtValidationException("Error validating token", e);
        }
    }

    public String extractUserId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.error("Error extracting user id from token: {}", e.getMessage());
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        return Objects.requireNonNull(extractExpiration(token)).before(new Date());
    }

    private Date extractExpiration(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();


        } catch (Exception e) {
            log.error("Error extracting expiration from token: {}", e.getMessage());
            return null;
        }
    }

    public Claims getClaimsFromToken(String token) {
        log.debug("Getting claims from token");
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error getting claims from token: {}", e.getMessage());
            throw new JwtValidationException("Error getting claims from token", e);
        }
    }

    public UserDTO convert(String token) {
        log.debug("Converting token to user");
        try {
            Claims claims = getClaimsFromToken(token);
            String email = claims.get("sub", String.class);
            String name = claims.get("name", String.class);
            List<String> roles = objectMapper.convertValue(claims.get("roles"), new TypeReference<>() {
            });
            return new UserDTO(email, name, roles.stream().map(RoleDTO::new).toList());
        } catch (Exception e) {
            log.error("Error converting token to user: {}", e.getMessage());
            throw new JwtValidationException("Error converting token to user", e);
        }
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error getting signing key: {}", e.getMessage());
            return null;
        }
    }
}
