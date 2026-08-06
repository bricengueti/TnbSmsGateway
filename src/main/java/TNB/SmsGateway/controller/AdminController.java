package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.*;
import TNB.SmsGateway.dto.response.*;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.DeviceType;
import TNB.SmsGateway.entity.PlanType;
import TNB.SmsGateway.entity.RoutingMode;
import TNB.SmsGateway.service.AdminPlatformService;
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
    private final AdminPlatformService adminPlatformService;   // ✅ ajouté au constructeur existant


    public AdminController(PlanService planService, AdminQuotaService adminQuotaService, AdminPlatformService adminPlatformService) {
        this.planService = planService;
        this.adminQuotaService = adminQuotaService;
        this.adminPlatformService = adminPlatformService;
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

    @Operation(summary = "Assigner un pack SMS (POOL)", description = "Définit la limite mensuelle de SMS " +
            "pour ce compte, avec reset automatique chaque mois. Remplace la limite précédente.")
    @PostMapping("/users/{userId}/quota/pool/assign-plan")
    public ResponseEntity<UserQuotaResponse> assignPoolPlan(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignPoolPlanRequest request
    ) {
        return ResponseEntity.ok(adminQuotaService.assignPoolPlan(userId, request.planId()));
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

// ===== PLATEFORME (vue d'ensemble) =====

    @Operation(summary = "Statistiques globales", description = "Vue d'ensemble de la plateforme : " +
            "utilisateurs, devices, clés API, messages envoyés.")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminPlatformService.getGlobalStats());
    }

// ===== USERS =====

    @Operation(summary = "Lister tous les utilisateurs")
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminPlatformService.listUsers());
    }

    @Operation(summary = "Détail d'un utilisateur", description = "Devices, clés API et quotas (POOL/PERSONAL) du compte.")
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(adminPlatformService.getUserDetail(id));
    }

    @Operation(summary = "Suspendre/réactiver un compte")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(adminPlatformService.updateUserStatus(id, request));
    }

// ===== DEVICES =====

    @Operation(summary = "Lister tous les devices", description = "Filtrable par type (PERSONAL/POOL) et statut.")
    @GetMapping("/devices")
    public ResponseEntity<List<AdminDeviceResponse>> listDevices(
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) DeviceStatus status
    ) {
        return ResponseEntity.ok(adminPlatformService.listDevices(type, status));
    }

    @Operation(summary = "Révoquer un device", description = "Révocation forcée par l'admin, peu importe le propriétaire.")
    @PostMapping("/devices/{id}/revoke")
    public ResponseEntity<ApiResponse> revokeDevice(@PathVariable UUID id) {
        adminPlatformService.revokeDevice(id);
        return ResponseEntity.ok(new ApiResponse("Device révoqué", true));
    }

// ===== API KEYS =====

    @Operation(summary = "Lister toutes les clés API", description = "Filtrable par routingMode.")
    @GetMapping("/api-keys")
    public ResponseEntity<List<AdminApiKeyResponse>> listApiKeys(
            @RequestParam(required = false) RoutingMode routingMode
    ) {
        return ResponseEntity.ok(adminPlatformService.listApiKeys(routingMode));
    }

    @Operation(summary = "Révoquer une clé API", description = "Révocation forcée par l'admin, peu importe le propriétaire.")
    @PostMapping("/api-keys/{id}/revoke")
    public ResponseEntity<ApiResponse> revokeApiKey(@PathVariable UUID id) {
        adminPlatformService.revokeApiKey(id);
        return ResponseEntity.ok(new ApiResponse("Clé API révoquée", true));
    }

    @Operation(summary = "Activer/désactiver le secours pool sur un device PERSONAL", description = "Permet " +
            "à un device BYOD de servir le pool partagé uniquement quand aucun device POOL pur n'est " +
            "disponible pour ce pays/opérateur. N'influence jamais le quota SMS du propriétaire du device.")
    @PatchMapping("/devices/{id}/pool-fallback")
    public ResponseEntity<AdminDeviceResponse> setPoolFallback(
            @PathVariable UUID id,
            @Valid @RequestBody DevicePoolFallbackRequest request
    ) {
        return ResponseEntity.ok(adminPlatformService.setPoolFallback(id, request.enabled()));
    }
}