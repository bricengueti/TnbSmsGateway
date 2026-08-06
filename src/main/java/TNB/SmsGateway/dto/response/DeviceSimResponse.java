package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse avec les informations d'une SIM")
public record DeviceSimResponse(
        @Schema(description = "ID de la SIM", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Index du slot", example = "0")
        Integer slotIndex,

        @Schema(description = "Code opérateur", example = "MTN_CM")
        String operatorCode,

        @Schema(description = "Nom de l'opérateur", example = "MTN")
        String operatorName,

        @Schema(description = "Numéro de téléphone", example = "+237699999999")
        String phoneNumber,

        @Schema(description = "SIM active", example = "true")
        Boolean isActive,

        @Schema(description = "SMS envoyés aujourd'hui", example = "42")
        Integer dailySmsSent,   // 🔥 corrigé : String → Integer

        @Schema(description = "Quota quotidien (nombre ou \"ILLIMITE\")", example = "100")
        String dailySmsQuota
) {}