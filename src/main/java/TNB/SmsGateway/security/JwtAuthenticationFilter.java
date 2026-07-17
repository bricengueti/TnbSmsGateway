package TNB.SmsGateway.security;

import TNB.SmsGateway.security.JwtAuthentication;
import TNB.SmsGateway.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Ignorer si c'est une ApiKey
            if (!token.startsWith("tnb_")) {
                try {
                    if (jwtUtils.isValidToken(token)) {
                        UUID userId = jwtUtils.getUserIdFromToken(token);
                        String email = jwtUtils.getEmailFromToken(token);

                        // Utiliser JwtAuthentication au lieu de UsernamePasswordAuthenticationToken
                        JwtAuthentication authentication = new JwtAuthentication(userId, email);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    logger.error("Invalid JWT token", e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}