package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Réponse détaillant un device")
public record DeviceResponse(
        @Schema(description = "ID du device", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Nom du device", example = "Téléphone Orange - bureau")
        String label,

        @Schema(description = "Code pays", example = "CM")
        String countryCode,

        @Schema(description = "Nom du pays", example = "Cameroun")
        String countryName,

        @Schema(description = "Statut actuel", example = "ONLINE")
        String status,

        @Schema(description = "Date de pairing")
        Instant pairedAt,

        @Schema(description = "Dernier heartbeat reçu")
        Instant lastHeartbeatAt,

        @Schema(description = "SIMs associées à ce device")
        List<DeviceSimResponse> sims

        // ❌ Retiré : String pairingCode
        // N'existe plus sur l'entité Device — le code de connexion vit
        // désormais au niveau compte (voir PairingCodeResponse ci-dessous).
) {}