package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.*;
import TNB.SmsGateway.dto.response.PlanResponse;
import TNB.SmsGateway.dto.response.UserQuotaResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.service.AdminQuotaService;
import TNB.SmsGateway.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final AdminQuotaService adminQuotaService;

    public AdminController(PlanService planService, AdminQuotaService adminQuotaService) {
        this.planService = planService;
        this.adminQuotaService = adminQuotaService;
    }

    // ===== PACKS =====

    @Operation(summary = "Créer un pack", description = "Crée un pack POOL (crédits SMS prépayés) ou " +
            "PERSONAL (plafond de devices).")
    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(201).body(planService.createPlan(request));
    }

    @Operation(summary = "Lister tous les packs")
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> listPlans() {
        return ResponseEntity.ok(planService.listAllPlans());
    }

    @Operation(summary = "Désactiver un pack", description = "Retire le pack de la vente. Les " +
            "utilisateurs qui l'ont déjà reçu conservent leur solde/plafond inchangé.")
    @PostMapping("/plans/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivatePlan(@PathVariable UUID id) {
        planService.deactivatePlan(id);
        return ResponseEntity.ok(new ApiResponse("Pack désactivé", true));
    }

    // ===== QUOTAS =====

    @Operation(summary = "Vue d'ensemble des quotas", description = "Liste tous les quotas d'un type " +
            "donné (POOL = crédits SMS, PERSONAL = plafond devices), pour le pilotage admin.")
    @GetMapping("/quotas")
    public ResponseEntity<List<UserQuotaResponse>> listQuotas(
            @Parameter(description = "Type de quota à lister", example = "POOL")
            @RequestParam PlanType type
    ) {
        return ResponseEntity.ok(adminQuotaService.listQuotas(type));
    }

    @Operation(summary = "Recharger des crédits SMS (pool prépayé)", description = "Ajoute des crédits " +
            "au solde existant — jamais un reset. Soit via un pack (planId), soit un montant libre " +
            "(credits). Le solde survit à toute régénération de clé API du client.")
    @PostMapping("/users/{userId}/quota/pool/topup")
    public ResponseEntity<UserQuotaResponse> topUpPool(
            @PathVariable UUID userId,
            @Valid @RequestBody TopUpPoolRequest request
    ) {
        return ResponseEntity.ok(adminQuotaService.topUpPool(userId, request));
    }

    @Operation(summary = "Assigner un pack de devices (PERSONAL)", description = "Définit le plafond " +
            "de devices personnels autorisés pour ce compte. Remplace la limite précédente.")
    @PostMapping("/users/{userId}/quota/personal/assign-plan")
    public ResponseEntity<UserQuotaResponse> assignPersonalPlan(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignPersonalPlanRequest request
    ) {
        return ResponseEntity.ok(adminQuotaService.assignPersonalPlan(userId, request.planId()));
    }

    @Operation(summary = "Ajuster manuellement un quota", description = "Geste commercial, " +
            "remboursement ou correction — définit des valeurs exactes (pas d'addition). " +
            "Le champ pertinent dépend du type: 'credits' pour POOL, 'maxDevices' pour PERSONAL.")
    @PatchMapping("/users/{userId}/quota/{type}/override")
    public ResponseEntity<UserQuotaResponse> overrideQuota(
            @PathVariable UUID userId,
            @PathVariable PlanType type,
            @Valid @RequestBody QuotaOverrideRequest request
    ) {
        return ResponseEntity.ok(adminQuotaService.overrideQuota(userId, type, request));
    }
}