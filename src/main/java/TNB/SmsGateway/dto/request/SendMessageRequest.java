package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'envoi d'un SMS unique")
public record SendMessageRequest(
        @Schema(description = "Numéro du destinataire au format E.164",
                example = "+237699999999",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+[1-9][0-9]{4,14}$",
                message = "Le numéro doit être au format E.164 (ex: +237699999999)")
        String to,

        @Schema(description = "Contenu du message",
                example = "Bonjour, votre commande est confirmée.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le corps du message est obligatoire")
        @Size(max = 160, message = "Le message ne doit pas dépasser 160 caractères pour un SMS standard")
        String body,

        @Schema(description = "Code pays ISO 3166-1 alpha-2",
                example = "CM",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le code pays est obligatoire")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Le code pays doit être au format ISO 3166-1 (2 lettres majuscules)")
        String countryCode,

        @Schema(description = "Code opérateur",
                example = "MTN_CM",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le code opérateur est obligatoire")
        String operator,

        @Schema(description = "Priorité du message",
                allowableValues = {"NORMAL", "HIGH"},
                example = "NORMAL")
        String priority,

        @Schema(description = "Clé d'idempotence pour éviter les doublons",
                example = "order-12345-1689012345")
        @Size(max = 255, message = "La clé d'idempotence ne doit pas dépasser 255 caractères")
        String idempotencyKey
) {}