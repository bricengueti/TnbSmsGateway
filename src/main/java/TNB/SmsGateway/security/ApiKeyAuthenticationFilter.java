package TNB.SmsGateway.security;

import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
import TNB.SmsGateway.repository.ApiKeyRepository;
import TNB.SmsGateway.service.CustomUserDetailsService;
import TNB.SmsGateway.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private final ApiKeyRepository apiKeyRepository;
    private final CustomUserDetailsService userDetailsService;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository,
                                      CustomUserDetailsService userDetailsService) {
        this.apiKeyRepository = apiKeyRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        log.info("🔍 ApiKeyFilter - Request: {} {} - Auth: {}", request.getMethod(), path, authHeader != null ? "présent" : "absent");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔍 ApiKeyFilter - Token reçu: {}...", token.substring(0, Math.min(30, token.length())));

            if (token.startsWith("tnb_")) {
                log.info("🔍 ApiKeyFilter - Traitement de la clé API");

                if (!token.startsWith("tnb_live_") && !token.startsWith("tnb_test_")) {
                    log.warn("❌ ApiKeyFilter - Format invalide");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"INVALID_API_KEY_FORMAT\"}");
                    return;
                }

                try {
                    // 🔥 RÉCUPÉRER TOUTES LES CLÉS ET VÉRIFIER AVEC BCrypt
                    List<ApiKey> apiKeys = apiKeyRepository.findAll();
                    ApiKey foundApiKey = null;

                    for (ApiKey apiKey : apiKeys) {
                        if (SecurityUtils.verify(token, apiKey.getKeyHash())) {
                            foundApiKey = apiKey;
                            log.info("✅ ApiKeyFilter - Clé trouvée par vérification BCrypt: id={}", apiKey.getId());
                            break;
                        }
                    }

                    if (foundApiKey == null) {
                        log.warn("❌ ApiKeyFilter - Aucune clé correspondante trouvée");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"INVALID_API_KEY\"}");
                        return;
                    }

                    log.info("✅ ApiKeyFilter - Clé trouvée: id={}, userId={}", foundApiKey.getId(), foundApiKey.getUser().getId());

                    if (foundApiKey.isRevoked()) {
                        log.warn("❌ ApiKeyFilter - Clé révoquée");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"API_KEY_REVOKED\"}");
                        return;
                    }

                    ApiKeyScope scope = foundApiKey.getScope();
                    if (!hasScopeAccess(scope, path, request.getMethod())) {
                        log.warn("❌ ApiKeyFilter - Scope insuffisant: {} pour {}", scope, path);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"INSUFFICIENT_SCOPE\"}");
                        return;
                    }

                    UserDetails userDetails = userDetailsService.loadUserById(foundApiKey.getUser().getId());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("✅ ApiKeyFilter - Authentification réussie pour userId={}", foundApiKey.getUser().getId());

                } catch (Exception e) {
                    log.error("❌ ApiKeyFilter - Erreur: {}", e.getMessage(), e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"INVALID_API_KEY\"}");
                    return;
                }
            } else {
                log.info("🔍 ApiKeyFilter - Token JWT détecté, ignoré par ApiKeyFilter");
            }
        } else {
            if (authHeader == null) {
                log.info("🔍 ApiKeyFilter - Pas de header Authorization");
            } else {
                log.warn("⚠️ ApiKeyFilter - Header Authorization présent mais préfixe invalide (attendu 'Bearer '): {}...",
                        authHeader.substring(0, Math.min(15, authHeader.length())));
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasScopeAccess(ApiKeyScope scope, String path, String method) {
        if (scope == ApiKeyScope.FULL) {
            return true;
        }

        if (scope == ApiKeyScope.SEND_ONLY) {
            return path.contains("/messages/send") && "POST".equalsIgnoreCase(method);
        }

        if (scope == ApiKeyScope.READ_ONLY) {
            return "GET".equalsIgnoreCase(method);
        }

        return false;
    }
}