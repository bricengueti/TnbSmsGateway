package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse de pairing d'un device — retournée à l'app mobile juste après " +
        "la soumission du code de connexion, pour finaliser l'enregistrement du téléphone.")
public record DevicePairResponse(
        @Schema(description = "ID du device nouvellement créé, à conserver côté app mobile pour la " +
                "connexion WebSocket ultérieure", example = "550e8400-e29b-41d4-a716-446655440000")
        String deviceId,

        @Schema(description = "Secret token du device, AFFICHÉ UNE SEULE FOIS — l'app mobile doit le " +
                "stocker de façon sécurisée (Keystore Android), il sert à authentifier la connexion " +
                "WebSocket du téléphone", example = "a1b2c3d4e5f6g7h8i9j0")
        String secretToken,

        @Schema(description = "Statut initial du device — toujours DISABLED à la création, bascule " +
                "automatiquement en ONLINE à la première connexion WebSocket réussie",
                example = "DISABLED", allowableValues = {"ONLINE", "OFFLINE", "BUSY", "DISABLED"})
        String status,

        @Schema(description = "Type de device, hérité du code de connexion scanné. PERSONAL = device " +
                "réservé au propriétaire du compte (usage BYOD classique). POOL = device contribué au " +
                "marketplace, pourra recevoir des SMS de clients tiers via leur clé API MANAGED_POOL.",
                example = "PERSONAL", allowableValues = {"PERSONAL", "POOL"})
        String deviceType
) {}