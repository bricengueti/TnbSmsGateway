package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Un message dans une requête bulk")
public record BulkMessageItem(
        @Schema(description = "Numéro du destinataire au format E.164",
                example = "+237699999999")
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+[1-9][0-9]{4,14}$",
                message = "Le numéro doit être au format E.164 (ex: +237699999999)")
        String to,

        @Schema(description = "Contenu du message", example = "Bonjour")
        @NotBlank(message = "Le corps du message est obligatoire")
        @Size(max = 160, message = "Le message ne doit pas dépasser 160 caractères")
        String body,

        @Schema(description = "Code pays", example = "CM")
        @NotBlank(message = "Le code pays est obligatoire")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Le code pays doit être au format ISO 3166-1")
        String countryCode,

        @Schema(description = "Code opérateur", example = "MTN_CM")
        @NotBlank(message = "Le code opérateur est obligatoire")
        String operator
) {}
