package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.response.UserQuotaResponse;
import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.AdminQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Auto-service : permet à l'utilisateur connecté de consulter SES PROPRES
 * quotas (POOL et PERSONAL), sans passer par les routes /admin/** qui
 * exigent le rôle ADMIN + un userId explicite.
 *
 * Ne duplique aucune logique métier : réutilise directement
 * AdminQuotaService.getUserQuotaOrNull(...) (voir AdminQuotaService_AJOUT.java) —
 * seule différence avec l'admin : le userId vient du JWT, pas d'un path variable.
 */
@RestController
@RequestMapping("/v1/me")
@Tag(name = "Mon compte", description = "Auto-service : quota et abonnement de l'utilisateur connecté")
@SecurityRequirement(name = "BearerAuth")
public class MeController {

    private final AdminQuotaService adminQuotaService;

    public MeController(AdminQuotaService adminQuotaService) {
        this.adminQuotaService = adminQuotaService;
    }

    @Operation(
            summary = "Mes quotas (POOL et PERSONAL)",
            description = "Retourne le quota courant de l'utilisateur connecté pour chaque type " +
                    "(consommation du mois, limite, plafond devices). Un champ est null si " +
                    "aucun pack de ce type n'a jamais été assigné à ce compte."
    )
    @GetMapping("/quota")
    public ResponseEntity<MyQuotaResponse> getMyQuota() {
        UUID userId = getCurrentUserId();

        UserQuotaResponse poolQuota = adminQuotaService.getUserQuotaOrNull(userId, PlanType.POOL);
        UserQuotaResponse personalQuota = adminQuotaService.getUserQuotaOrNull(userId, PlanType.PERSONAL);

        return ResponseEntity.ok(new MyQuotaResponse(poolQuota, personalQuota));
    }

    // --- même pattern que dans ApiKeyController.getCurrentUserId(), copié tel quel ---
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        throw new RuntimeException("Impossible de déterminer l'utilisateur courant");
    }

    /** DTO minimal — devices/apiKeys déjà disponibles via /v1/devices et /v1/api-keys. */
    public record MyQuotaResponse(UserQuotaResponse poolQuota, UserQuotaResponse personalQuota) {}
}
