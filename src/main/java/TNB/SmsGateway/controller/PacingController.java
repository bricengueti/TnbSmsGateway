package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.dto.request.DevicePacingRequest;
import TNB.SmsGateway.dto.request.SimPacingRequest;
import TNB.SmsGateway.service.PacingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ⚠️ Endpoints volontairement PUBLICS (voir SecurityConfig) : appelés
 * directement par l'app mobile, qui n'a pas de JWT utilisateur. Le
 * deviceId dans l'URL suffit à retrouver le propriétaire (device.getUser())
 * côté service — pas de vérification d'identité supplémentaire ici.
 *
 * Risque accepté : n'importe qui connaissant un deviceId (UUID, non
 * énumérable en pratique) peut modifier sa cadence d'envoi. Impact limité
 * puisque cette valeur ne fait qu'espacer des SMS déjà légitimement en
 * file — elle ne permet ni de lire, ni d'envoyer, ni de détourner des
 * messages.
 */
@RestController
@RequestMapping("/v1/devices")
@Tag(name = "Cadence d'envoi", description = "Réglages anti-détection robot (délai entre SMS)")
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
            @Valid @RequestBody DevicePacingRequest request
    ) {
        pacingService.updateDevicePacing(deviceId, request);
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
            @Valid @RequestBody SimPacingRequest request
    ) {
        pacingService.updateSimPacing(deviceId, simId, request);
        return ResponseEntity.ok(new ApiResponse("Cadence de la SIM mise à jour avec succès", true));
    }
}