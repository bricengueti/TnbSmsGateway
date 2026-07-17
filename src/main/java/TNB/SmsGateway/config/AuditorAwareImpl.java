package TNB.SmsGateway.config;
import TNB.SmsGateway.security.JwtAuthentication;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // JWT Authentication (Dashboard)
        if (authentication instanceof JwtAuthentication) {
            JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
            return Optional.of(jwtAuth.getUserId());
        }

        // ApiKey Authentication (Intégration)
        if (authentication instanceof ApiKeyAuthentication) {
            ApiKeyAuthentication apiKeyAuth = (ApiKeyAuthentication) authentication;
            return Optional.of(apiKeyAuth.getUserId());
        }

        // Principal est directement l'UUID
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            return Optional.of((UUID) principal);
        }

        return Optional.empty();
    }
}
