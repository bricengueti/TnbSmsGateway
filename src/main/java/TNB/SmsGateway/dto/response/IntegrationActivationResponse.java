package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'activation de l'intégration (clé API + code de connexion générés ensemble)")
public record IntegrationActivationResponse(
        @Schema(description = "ID de la clé API créée", example = "550e8400-e29b-41d4-a716-446655440000")
        String apiKeyId,

        @Schema(description = "Clé API complète (AFFICHÉE UNE SEULE FOIS)",
                example = "tnb_live_7f2k9xq1m3p5v8d2c4a6")
        String apiKey,

        @Schema(description = "Préfixe de la clé (visible ensuite dans le dashboard)",
                example = "tnb_live_7f2k9x...")
        String apiKeyPrefix,

        @Schema(description = "Code de connexion à saisir sur chaque téléphone-passerelle (AFFICHÉ UNE SEULE FOIS)",
                example = "482913")
        String pairingCode
) {}