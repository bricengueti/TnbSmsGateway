package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.AssignPlanRequest;
import TNB.SmsGateway.dto.request.PlanRequest;
import TNB.SmsGateway.dto.request.QuotaOverrideRequest;
import TNB.SmsGateway.dto.response.ManagedApiKeyResponse;
import TNB.SmsGateway.dto.response.PlanResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.service.AdminApiKeyService;
import TNB.SmsGateway.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Administration", description = "Gestion des packs et des quotas — réservé aux comptes admin")
public class AdminController {

    private final PlanService planService;
    private final AdminApiKeyService adminApiKeyService;

    public AdminController(PlanService planService, AdminApiKeyService adminApiKeyService) {
        this.planService = planService;
        this.adminApiKeyService = adminApiKeyService;
    }

    // ===== PACKS =====

    @Operation(summary = "Créer un pack", description = "Crée un nouveau pack tarifaire (ex: Starter, " +
            "Pro, Illimité) qui pourra ensuite être assigné à des clés API en mode pool partagé.")
    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    @Operation(summary = "Lister tous les packs", description = "Retourne tous les packs, actifs et " +
            "inactifs, pour la vue d'administration.")
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> listPlans() {
        return ResponseEntity.ok(planService.listAllPlans());
    }

    @Operation(summary = "Désactiver un pack", description = "Retire le pack de la liste proposée aux " +
            "nouveaux clients. Les clients qui l'ont déjà assigné conservent leur quota inchangé.")
    @PostMapping("/plans/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivatePlan(@PathVariable UUID id) {
        planService.deactivatePlan(id);
        return ResponseEntity.ok(new ApiResponse("Pack désactivé", true));
    }

    // ===== QUOTAS / CLÉS MANAGED_POOL =====

    @Operation(summary = "Vue d'ensemble des clés en mode pool partagé", description = "Liste toutes " +
            "les clés API en routingMode=MANAGED_POOL, avec leur pack assigné, leur consommation du " +
            "mois en cours et le pourcentage utilisé — pour le pilotage admin.")
    @GetMapping("/api-keys/managed")
    public ResponseEntity<List<ManagedApiKeyResponse>> listManagedApiKeys() {
        return ResponseEntity.ok(adminApiKeyService.listManagedApiKeys());
    }

    @Operation(summary = "Assigner un pack à une clé API", description = "Applique les limites du pack " +
            "choisi à cette clé (routingMode doit être MANAGED_POOL). Écrase toute limite précédemment " +
            "définie manuellement sur cette clé.")
    @PostMapping("/api-keys/{id}/assign-plan")
    public ResponseEntity<ManagedApiKeyResponse> assignPlan(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPlanRequest request
    ) {
        return ResponseEntity.ok(adminApiKeyService.assignPlan(id, request.planId()));
    }

    @Operation(summary = "Ajuster manuellement le quota d'une clé", description = "Geste commercial ou " +
            "dépannage ponctuel — surcharge la limite ou le compteur en dehors de tout pack. Le pack " +
            "assigné (s'il existe) n'est pas modifié, seule cette clé est affectée.")
    @PatchMapping("/api-keys/{id}/quota-override")
    public ResponseEntity<ManagedApiKeyResponse> overrideQuota(
            @PathVariable UUID id,
            @Valid @RequestBody QuotaOverrideRequest request
    ) {
        return ResponseEntity.ok(adminApiKeyService.overrideQuota(id, request));
    }
}