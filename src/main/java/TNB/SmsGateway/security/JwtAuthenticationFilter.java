package TNB.SmsGateway.security;

import TNB.SmsGateway.service.CustomUserDetailsService;
import TNB.SmsGateway.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        log.info("🔍 Request: {} {} - Auth: {}", request.getMethod(), path, authHeader != null ? "présent" : "absent");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔍 Token: {}...", token.substring(0, Math.min(20, token.length())));

            // Ignorer si c'est une ApiKey
            if (!token.startsWith("tnb_")) {
                try {
                    log.info("🔍 Validation du token JWT...");

                    if (jwtUtils.isValidToken(token)) {
                        UUID userId = jwtUtils.getUserIdFromToken(token);
                        String email = jwtUtils.getEmailFromToken(token);

                        log.info("✅ Token valide: userId={}, email={}", userId, email);

                        // 🔥 Charger l'utilisateur via CustomUserDetailsService
                        UserDetails userDetails = userDetailsService.loadUserById(userId);
                        log.info("✅ UserDetails chargé: {}", userDetails.getUsername());

                        // 🔥 Créer l'authentification Spring
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("✅ Authentification définie pour l'utilisateur: {}", email);
                    } else {
                        log.warn("❌ Token JWT invalide pour {}", path);
                    }
                } catch (Exception e) {
                    log.error("❌ Erreur JWT: {}", e.getMessage(), e);
                }
            } else {
                log.info("🔑 Token ApiKey détecté, ignoré par JwtAuthenticationFilter");
            }
        }

        filterChain.doFilter(request, response);
    }
}