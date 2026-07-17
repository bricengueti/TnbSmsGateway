package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.DevicePairRequest;
import TNB.SmsGateway.dto.request.DeviceRegisterRequest;
import TNB.SmsGateway.dto.request.DeviceUpdateRequest;
import TNB.SmsGateway.dto.response.DevicePairResponse;
import TNB.SmsGateway.dto.response.DeviceResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.service.DevicePairingService;
import TNB.SmsGateway.service.DeviceService;
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

/**
 * CONTROLLER: DeviceController
 *
 * DESCRIPTION: Gère les devices Android (SMS gateways)
 * URL de base: /api/v1/devices (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. L'utilisateur enregistre un device → génère un code de pairing
 * 2. L'app mobile soumet le code de pairing
 * 3. L'utilisateur liste ses devices
 * 4. L'utilisateur modifie un device
 * 5. L'utilisateur supprime un device
 */
@RestController
@RequestMapping("/v1/devices")
@Tag(name = "Devices", description = "Gestion des devices Android (SMS gateways)")
public class DeviceController {

    private final DeviceService deviceService;
    private final DevicePairingService devicePairingService;

    public DeviceController(DeviceService deviceService, DevicePairingService devicePairingService) {
        this.deviceService = deviceService;
        this.devicePairingService = devicePairingService;
    }

    // =============================================
    // ===== ENREGISTREMENT (Dashboard) =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur enregistre un device
     *
     * URL: POST /api/v1/devices/register
     * Auth: JWT
     *
     * RÉPONSES:
     * - 201: Device créé avec code de pairing
     * - 404: Pays non trouvé
     * - 401: Token invalide
     */
    @Operation(
            summary = "Enregistrer un device",
            description = "Crée un nouveau device et génère un code de pairing à 6 chiffres valable 15 minutes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Device créé avec succès",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Pays non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/register")
    public ResponseEntity<DeviceResponse> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        DeviceResponse response = deviceService.registerDevice(userId, request);
        return ResponseEntity.status(201).body(response);
    }

    // =============================================
    // ===== PAIRING (App mobile) =====
    // =============================================

    /**
     * SCÉNARIO: L'app mobile soumet le code de pairing
     *
     * URL: POST /api/v1/devices/pair
     * Auth: Aucune (code de pairing)
     *
     * RÉPONSES:
     * - 200: Pairing réussi → secretToken
     * - 400: Code invalide ou expiré
     */
    @Operation(
            summary = "Pairing d'un device",
            description = "L'app mobile soumet le code de pairing pour finaliser l'enregistrement. Retourne un secretToken pour la connexion WebSocket."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pairing réussi",
                    content = @Content(schema = @Schema(implementation = DevicePairResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Code de pairing invalide ou expiré"
            )
    })
    @PostMapping("/pair")
    public ResponseEntity<DevicePairResponse> pairDevice(
            @Valid @RequestBody DevicePairRequest request
    ) {
        DevicePairResponse response = devicePairingService.pairDevice(request);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== LISTE DES DEVICES =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur liste ses devices
     *
     * URL: GET /api/v1/devices
     * Auth: JWT ou ApiKey
     *
     * RÉPONSES:
     * - 200: Liste des devices
     * - 401: Token invalide
     */
    @Operation(
            summary = "Lister les devices",
            description = "Retourne la liste de tous les devices de l'utilisateur avec leurs SIMs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token invalide"
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "ApiKeyAuth")
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> listDevices(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<DeviceResponse> devices = deviceService.listDevices(userId);
        return ResponseEntity.ok(devices);
    }

    // =============================================
    // ===== DÉTAIL D'UN DEVICE =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur consulte les détails d'un device
     *
     * URL: GET /api/v1/devices/{id}
     * Auth: JWT ou ApiKey
     */
    @Operation(
            summary = "Détail d'un device",
            description = "Retourne les détails d'un device spécifique avec ses SIMs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token invalide"
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "ApiKeyAuth")
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        DeviceResponse response = deviceService.getDevice(userId, id);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== MISE À JOUR =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur modifie un device
     *
     * URL: PATCH /api/v1/devices/{id}
     * Auth: JWT
     */
    @Operation(
            summary = "Modifier un device",
            description = "Met à jour le label ou le pays d'un device."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device mis à jour"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceUpdateRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        DeviceResponse response = deviceService.updateDevice(userId, id, request);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== SUPPRESSION =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur supprime un device
     *
     * URL: DELETE /api/v1/devices/{id}
     * Auth: JWT
     */
    @Operation(
            summary = "Supprimer un device",
            description = "Supprime un device (soft delete). Les messages en cours seront réassignés ou échoueront."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device supprimé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDevice(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        deviceService.deleteDevice(userId, id);
        return ResponseEntity.ok(new ApiResponse("Device supprimé avec succès", true));
    }
}