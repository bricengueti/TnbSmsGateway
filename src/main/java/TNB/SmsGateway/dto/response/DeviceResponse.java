package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Réponse avec les informations d'un device")
public record DeviceResponse(
        @Schema(description = "ID du device", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Nom du device", example = "Device Cameroun 1")
        String label,

        @Schema(description = "Code pays", example = "CM")
        String countryCode,

        @Schema(description = "Nom du pays", example = "Cameroun")
        String countryName,

        @Schema(description = "Statut du device", example = "ONLINE")
        String status,

        @Schema(description = "Date de pairing", example = "2026-07-16T14:30:00Z")
        Instant pairedAt,

        @Schema(description = "Dernier heartbeat", example = "2026-07-16T14:35:00Z")
        Instant lastHeartbeatAt,

        @Schema(description = "Liste des SIMs")
        List<DeviceSimResponse> sims,

        @Schema(description = "Code de pairing (valable 15 minutes)", example = "654321")
        String pairingCode
) {}