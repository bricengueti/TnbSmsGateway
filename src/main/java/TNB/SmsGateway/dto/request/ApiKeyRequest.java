package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de création d'une clé API")
public record ApiKeyRequest(
        @Schema(description = "Label descriptif de la clé", example = "Application de notification - Production")
        @NotBlank(message = "Le label est obligatoire")
        String label,

        @Schema(description = "Scope de la clé API",
                allowableValues = {"FULL", "SEND_ONLY", "READ_ONLY"},
                example = "SEND_ONLY")
        @NotNull(message = "Le scope est obligatoire")
        String scope
) {}