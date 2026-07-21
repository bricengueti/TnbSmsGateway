package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.DevicePairRequest;
import TNB.SmsGateway.dto.request.DeviceUpdateRequest;
import TNB.SmsGateway.dto.response.DevicePairResponse;
import TNB.SmsGateway.dto.response.DeviceResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.security.UserPrincipal;
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

    // ❌ Supprimé : POST /register
    // La création d'un device ne se fait plus manuellement depuis le
    // dashboard avec un code jetable — elle se fait automatiquement
    // au moment du pairing (voir IntegrationController.activate() pour
    // générer le code de connexion, et /pair ci-dessous pour l'utiliser).

    // =============================================
    // ===== PAIRING (App mobile) =====
    // =============================================

    @Operation(
            summary = "Pairing d'un device",
            description = "L'app mobile soumet le code de connexion du compte (réutilisable sur plusieurs " +
                    "devices) ainsi que le pays détecté sur la SIM. Un nouveau device est créé à cet instant."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pairing réussi",
                    content = @Content(schema = @Schema(implementation = DevicePairResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Code de connexion invalide/révoqué, ou pays non supporté"
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
        UUID userId = getUserIdFromAuthentication(authentication);
        List<DeviceResponse> devices = deviceService.listDevices(userId);
        return ResponseEntity.ok(devices);
    }

    // =============================================
    // ===== DÉTAIL D'UN DEVICE =====
    // =============================================

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
        UUID userId = getUserIdFromAuthentication(authentication);
        DeviceResponse response = deviceService.getDevice(userId, id);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== MISE À JOUR =====
    // =============================================

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
        UUID userId = getUserIdFromAuthentication(authentication);
        DeviceResponse response = deviceService.updateDevice(userId, id, request);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== RÉVOCATION (nouveau) =====
    // =============================================

    @Operation(
            summary = "Révoquer un device",
            description = "Bloque immédiatement ce device précis (ne peut plus se connecter, n'apparaît plus " +
                    "comme disponible pour l'envoi de SMS) sans affecter le code de connexion du compte ni les " +
                    "autres devices déjà pairés avec ce même code."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device révoqué"
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
    @PostMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse> revokeDevice(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        deviceService.revokeDevice(userId, id);
        return ResponseEntity.ok(new ApiResponse("Device révoqué avec succès", true));
    }

    // =============================================
    // ===== SUPPRESSION =====
    // =============================================

    @Operation(
            summary = "Supprimer un device",
            description = "Supprime un device (soft delete)."
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
        UUID userId = getUserIdFromAuthentication(authentication);
        deviceService.deleteDevice(userId, id);
        return ResponseEntity.ok(new ApiResponse("Device supprimé avec succès", true));
    }

    // =============================================
    // ===== UTILITAIRE =====
    // =============================================

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
}