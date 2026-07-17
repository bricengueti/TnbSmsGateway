package TNB.SmsGateway.security;
import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.utils.SecurityUtils;
import TNB.SmsGateway.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Vérifier si c'est une ApiKey
            if (token.startsWith("tnb_")) {
                try {
                    if (!token.startsWith("tnb_live_") && !token.startsWith("tnb_test_")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"INVALID_API_KEY_FORMAT\"}");
                        return;
                    }

                    String hashedKey = SecurityUtils.hash(token);
                    ApiKey apiKey = apiKeyRepository.findByKeyHash(hashedKey).orElse(null);

                    if (apiKey == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"INVALID_API_KEY\"}");
                        return;
                    }

                    if (apiKey.isRevoked()) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"API_KEY_REVOKED\"}");
                        return;
                    }

                    // Vérifier scope
                    String path = request.getRequestURI();
                    ApiKeyScope scope = apiKey.getScope();

                    if (!hasScopeAccess(scope, path, request.getMethod())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"INSUFFICIENT_SCOPE\"}");
                        return;
                    }

                    User user = apiKey.getUser();

                    // Utiliser ApiKeyAuthentication
                    ApiKeyAuthentication authentication = new ApiKeyAuthentication(
                            user.getId(),
                            apiKey.getId().toString(),
                            scope
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"INVALID_API_KEY\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasScopeAccess(ApiKeyScope scope, String path, String method) {
        if (scope == ApiKeyScope.FULL) return true;

        if (scope == ApiKeyScope.SEND_ONLY) {
            return path.contains("/messages/send") && "POST".equalsIgnoreCase(method);
        }

        if (scope == ApiKeyScope.READ_ONLY) {
            return "GET".equalsIgnoreCase(method);
        }

        return false;
    }
}