package dow.security;

import dow.exception.JwtAuthenticationException;
import dow.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtUtils {


    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userPrincipal)) {
            throw new IllegalArgumentException("Principal is not CustomUserDetails");
        }

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
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

        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();

        List<?> rawRoles = claims.get("roles", List.class);
        List<GrantedAuthority> authorities = rawRoles == null
                ? List.of()
                : rawRoles.stream()
                .filter(r -> r instanceof String)
                .map(r -> new SimpleGrantedAuthority((String) r))
                .collect(Collectors.toList());

        UserDetails userDetails = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}