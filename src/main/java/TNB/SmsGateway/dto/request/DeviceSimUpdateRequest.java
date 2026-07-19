package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de mise à jour d'une SIM")
public record DeviceSimUpdateRequest(
        @Schema(description = "Code opérateur", example = "MTN_CM")
        @Size(max = 50, message = "Le code opérateur ne doit pas dépasser 50 caractères")
        String operatorCode,

        @Schema(description = "Activer ou désactiver la SIM", example = "true")
        Boolean isActive,

        @Schema(description = "Quota quotidien de SMS : un nombre positif ou \"ILLIMITE\"", example = "50")
        @Size(max = 20, message = "Le quota ne doit pas dépasser 20 caractères")
        String dailyQuota
) {}