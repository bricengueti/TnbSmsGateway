package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'activation de l'intégration")
public record IntegrationActivateRequest(
        @Schema(description = "Label optionnel pour la clé API générée", example = "Application de production")
        @Size(max = 100, message = "Le label ne doit pas dépasser 100 caractères")
        String apiKeyLabel
) {}