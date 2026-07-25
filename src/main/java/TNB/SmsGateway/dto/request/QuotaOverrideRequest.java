package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête d'ajustement manuel d'un quota, en dehors de tout pack")
public record QuotaOverrideRequest(
        @Schema(description = "Rendre la clé illimitée, indépendamment du pack assigné", example = "false")
        Boolean unlimited,

        @Schema(description = "Nouvelle limite mensuelle. Ignoré si unlimited=true.", example = "10000")
        Integer monthlyLimit,

        @Schema(description = "Réinitialiser le compteur du mois en cours à 0", example = "false")
        Boolean resetCounter
) {}