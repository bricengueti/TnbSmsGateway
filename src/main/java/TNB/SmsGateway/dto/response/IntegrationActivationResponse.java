package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Réponse d'activation de l'intégration (clé API + code de connexion générés ensemble)")
public record IntegrationActivationResponse(
        @Schema(description = "ID de la clé API créée (null si withApiKey=false)")
        String apiKeyId,

        @Schema(description = "Clé API complète, AFFICHÉE UNE SEULE FOIS (null si withApiKey=false)")
        String apiKey,

        @Schema(description = "Préfixe de la clé (null si withApiKey=false)")
        String apiKeyPrefix,

        @Schema(description = "Code de connexion à saisir sur chaque téléphone-passerelle (AFFICHÉ UNE SEULE FOIS)")
        String pairingCode,

        @Schema(description = "Type de device ciblé par ce code de connexion", example = "POOL")
        String targetType
) {}