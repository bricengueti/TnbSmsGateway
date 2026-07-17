package TNB.SmsGateway.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Réponse avec les informations d'un message")
public record MessageResponse(
        @Schema(description = "ID du message", example = "550e8400-e29b-41d4-a716-446655440000")
        String messageId,

        @Schema(description = "Direction", example = "OUTBOUND")
        String direction,

        @Schema(description = "Numéro de destination", example = "+237699999999")
        String toNumber,

        @Schema(description = "Numéro source", example = "+237612345678")
        String fromNumber,

        @Schema(description = "Contenu du message", example = "Bonjour")
        String body,

        @Schema(description = "Code pays", example = "CM")
        String countryCode,

        @Schema(description = "Code opérateur", example = "MTN_CM")
        String operatorCode,

        @Schema(description = "Statut du message", example = "DELIVERED")
        String status,

        @Schema(description = "Nombre de tentatives", example = "1")
        Integer attempts,

        @Schema(description = "Raison de l'erreur", example = "Device offline")
        String errorReason,

        @Schema(description = "ID du device", example = "550e8400-e29b-41d4-a716-446655440000")
        String deviceId,

        @Schema(description = "Date de création", example = "2026-07-16T14:30:00Z")
        Instant createdAt,

        @Schema(description = "Date de dispatch", example = "2026-07-16T14:30:05Z")
        Instant dispatchedAt,

        @Schema(description = "Date de délivrance", example = "2026-07-16T14:30:10Z")
        Instant deliveredAt
) {}