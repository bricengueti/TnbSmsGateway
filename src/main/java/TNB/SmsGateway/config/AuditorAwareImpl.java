package TNB.SmsGateway.config;

import TNB.SmsGateway.security.UserPrincipal;
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

        Object principal = authentication.getPrincipal();

        // 🔥 Si c'est un UserPrincipal, récupérer l'ID
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            return Optional.of(userPrincipal.getId());
        }

        // Si le principal est directement un UUID
        if (principal instanceof UUID) {
            return Optional.of((UUID) principal);
        }

        return Optional.empty();
    }
}