package dow.security;

import dow.exception.JwtAuthenticationException;
import dow.model.CustomUserDetails;
import dow.service.PlayerService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtUtils {

    private final PlayerService playerService;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public JwtUtils(PlayerService playerService) {
        this.playerService = playerService;
    }

    public String generateJwtToken(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication or principal is null");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userPrincipal)) {
            throw new IllegalArgumentException("Principal is not CustomUserDetails");
        }

        Instant now = Instant.now();
        Instant expiryInstant = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("INVALID_OR_EXPIRED_TOKEN");
        }
    }

    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken getAuthenticationFromToken(String token) {
        if (token == null || !validateJwtToken(token)) {
            throw new JwtAuthenticationException("INVALID_OR_EXPIRED_TOKEN");
        }
        String username = getUserNameFromJwtToken(token);
        UserDetails userDetails = playerService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}