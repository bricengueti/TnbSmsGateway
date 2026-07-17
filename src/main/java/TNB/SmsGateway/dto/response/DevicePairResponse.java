package TNB.SmsGateway.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse de pairing d'un device")
public record DevicePairResponse(
        @Schema(description = "ID du device", example = "550e8400-e29b-41d4-a716-446655440000")
        String deviceId,

        @Schema(description = "Secret token (à stocker côté app mobile)",
                example = "a1b2c3d4e5f6g7h8i9j0")
        String secretToken,

        @Schema(description = "Statut du device", example = "OFFLINE")
        String status
) {}