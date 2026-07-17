package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.DeviceSimUpdateRequest;
import TNB.SmsGateway.dto.response.DeviceSimResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.DeviceService;
import TNB.SmsGateway.service.DeviceSimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CONTROLLER: DeviceSimController
 *
 * DESCRIPTION: Gère les SIMs des devices
 * URL de base: /api/v1/devices/{deviceId}/sims (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. L'utilisateur liste les SIMs d'un device
 * 2. L'utilisateur active/désactive une SIM
 * 3. L'utilisateur modifie le quota quotidien d'une SIM
 * 4. L'utilisateur modifie l'opérateur d'une SIM
 */
@RestController
@RequestMapping("/v1/devices/{deviceId}/sims")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "SIMs", description = "Gestion des SIMs des devices")
public class DeviceSimController {

    private final DeviceSimService deviceSimService;
    private final DeviceService deviceService;

    public DeviceSimController(DeviceSimService deviceSimService,
                               DeviceService deviceService) {
        this.deviceSimService = deviceSimService;
        this.deviceService = deviceService;
    }

    // =============================================
    // ===== LISTER LES SIMS D'UN DEVICE =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur consulte les SIMs d'un device
     *
     * URL: GET /api/v1/devices/{deviceId}/sims
     * Auth: JWT
     *
     * RÉPONSES:
     * - 200: Liste des SIMs
     * - 404: Device non trouvé
     * - 403: Accès non autorisé
     * - 401: Token invalide
     */
    @Operation(
            summary = "Lister les SIMs d'un device",
            description = "Retourne la liste de toutes les SIMs d'un device spécifique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = DeviceSimResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @GetMapping
    public ResponseEntity<List<DeviceSimResponse>> listSims(
            @PathVariable UUID deviceId,
            Authentication authentication
    ) {
        // 1. Récupérer l'ID utilisateur
        UUID userId = getUserIdFromAuthentication(authentication);

        // 2. Vérifier que le device appartient à l'utilisateur
        Device device = deviceService.findByIdAndUser(deviceId, userId);

        // 3. Récupérer les SIMs
        List<DeviceSim> sims = deviceSimService.getSimsByDevice(device);

        // 4. Convertir en réponse
        List<DeviceSimResponse> response = sims.stream()
                .map(this::toDeviceSimResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== METTRE À JOUR UNE SIM =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur modifie les paramètres d'une SIM
     *
     * URL: PATCH /api/v1/devices/{deviceId}/sims/{simId}
     * Auth: JWT
     *
     * RÉPONSES:
     * - 200: SIM mise à jour avec succès
     * - 404: SIM ou device non trouvé
     * - 403: Accès non autorisé
     * - 400: Erreur de validation (opérateur ne correspond pas au pays)
     * - 401: Token invalide
     */
    @Operation(
            summary = "Mettre à jour une SIM",
            description = "Modifie l'opérateur, active/désactive une SIM ou modifie son quota quotidien."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SIM mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = DeviceSimResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "SIM ou device non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "L'opérateur n'appartient pas au pays du device"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @PatchMapping("/{simId}")
    public ResponseEntity<DeviceSimResponse> updateSim(
            @PathVariable UUID deviceId,
            @PathVariable UUID simId,
            @Valid @RequestBody DeviceSimUpdateRequest request,
            Authentication authentication
    ) {
        // 1. Récupérer l'ID utilisateur
        UUID userId = getUserIdFromAuthentication(authentication);

        // 2. Mettre à jour la SIM
        DeviceSim sim = deviceSimService.updateSim(
                userId,
                simId,
                request.operatorCode(),
                request.isActive(),
                request.dailyQuota()
        );

        // 3. Convertir en réponse
        return ResponseEntity.ok(toDeviceSimResponse(sim));
    }

    // =============================================
    // ===== UTILITAIRES =====
    // =============================================

    /**
     * Extraire l'ID utilisateur de l'authentification
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new RuntimeException("Impossible d'extraire l'ID utilisateur");
    }

    /**
     * Convertir DeviceSim en DeviceSimResponse
     */
    private DeviceSimResponse toDeviceSimResponse(DeviceSim sim) {
        return new DeviceSimResponse(
                sim.getId().toString(),
                sim.getSlotIndex(),
                sim.getOperator().getCode(),
                sim.getOperator().getDisplayName(),
                sim.getPhoneNumber(),
                sim.getIsActive(),
                sim.getDailySmsSent(),
                sim.getDailySmsQuota()
        );
    }
}