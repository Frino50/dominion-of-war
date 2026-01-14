package perso.dow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import perso.dow.exception.JwtAuthenticationException;

import java.io.IOException;

@Component
public class FilterInternal extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public FilterInternal(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtUtils.extractToken(request.getHeader("Authorization"));
            if (token != null) {
                UsernamePasswordAuthenticationToken authentication = jwtUtils.getAuthenticationFromToken(token);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtAuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"INVALID_OR_EXPIRED_TOKEN\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
