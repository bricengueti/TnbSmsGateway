package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de rechargement de crédits SMS (pool prépayé)")
public record TopUpPoolRequest(
        @Schema(description = "ID d'un pack POOL à appliquer (ajoute son nombre de crédits au solde " +
                "existant). Optionnel si 'credits' est renseigné directement.", example = "550e8400-...")
        String planId,

        @Schema(description = "Montant de crédits à ajouter manuellement, indépendamment de tout pack " +
                "(rechargement libre, geste commercial). Ignoré si planId est fourni.", example = "1000")
        Integer credits
) {}