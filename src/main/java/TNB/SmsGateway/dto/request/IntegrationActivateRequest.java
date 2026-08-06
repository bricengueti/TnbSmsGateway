package TNB.SmsGateway.dto.request;

import TNB.SmsGateway.entity.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'activation de l'intégration")
public record IntegrationActivateRequest(
        @Schema(description = "Label optionnel pour la clé API générée", example = "Application de production")
        @Size(max = 100, message = "Le label ne doit pas dépasser 100 caractères")
        String apiKeyLabel,

        @Schema(description = "Type de device visé par le code de connexion. PERSONAL par défaut.",
                example = "POOL")
        DeviceType targetType,

        @Schema(description = "Générer aussi une clé API en même temps que le code de connexion. " +
                "true par défaut — à mettre à false pour un contributeur qui ne fait que prêter son device.",
                example = "false")
        Boolean withApiKey
) {
    // Normalisation : record "brut" tel que reçu du JSON, avec des defaults appliqués
    // à la lecture plutôt que dans le constructeur canonique (pour rester un record simple).
    public DeviceType resolvedTargetType() {
        return targetType != null ? targetType : DeviceType.PERSONAL;
    }

    public boolean resolvedWithApiKey() {
        return withApiKey == null || withApiKey;
    }
}