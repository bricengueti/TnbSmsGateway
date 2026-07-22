package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.dto.request.DevicePacingRequest;
import TNB.SmsGateway.dto.request.SimPacingRequest;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.PacingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/devices")
@Tag(name = "Cadence d'envoi", description = "Réglages anti-détection robot (délai entre SMS)")
@SecurityRequirement(name = "BearerAuth")
public class PacingController {

    private final PacingService pacingService;

    public PacingController(PacingService pacingService) {
        this.pacingService = pacingService;
    }

    @Operation(
            summary = "Régler la cadence par défaut d'un device",
            description = "Définit le délai minimum/maximum (secondes) entre 2 SMS envoyés depuis ce " +
                    "téléphone, pour éviter qu'un opérateur ne détecte un envoi automatisé et ne bloque la SIM."
    )
    @PatchMapping("/{deviceId}/pacing")
    public ResponseEntity<ApiResponse> updateDevicePacing(
            @PathVariable UUID deviceId,
            @Valid @RequestBody DevicePacingRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        pacingService.updateDevicePacing(userId, deviceId, request);
        return ResponseEntity.ok(new ApiResponse("Cadence du device mise à jour avec succès", true));
    }

    @Operation(
            summary = "Surcharger la cadence d'une SIM précise",
            description = "Définit une cadence spécifique pour cette SIM, différente de celle par défaut " +
                    "du device. Envoyer null pour minDelaySec/maxDelaySec retire la surcharge."
    )
    @PatchMapping("/{deviceId}/sims/{simId}/pacing")
    public ResponseEntity<ApiResponse> updateSimPacing(
            @PathVariable UUID deviceId,
            @PathVariable UUID simId,
            @Valid @RequestBody SimPacingRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        pacingService.updateSimPacing(userId, deviceId, simId, request);
        return ResponseEntity.ok(new ApiResponse("Cadence de la SIM mise à jour avec succès", true));
    }

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