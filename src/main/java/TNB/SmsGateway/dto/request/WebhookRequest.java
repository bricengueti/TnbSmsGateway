package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Requête de configuration du webhook")
public record WebhookRequest(
        @Schema(description = "URL du webhook pour recevoir les SMS entrants",
                example = "https://mon-entreprise.com/api/sms-reception")
        @Pattern(regexp = "^(https?://)[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+$",
                message = "Format d'URL invalide. Utilisez http:// ou https://")
        String webhookUrl
) {}