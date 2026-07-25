package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Détails d'un pack (plan tarifaire)")
public record PlanResponse(
        @Schema(description = "ID du pack", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Nom commercial", example = "Pro")
        String name,

        @Schema(description = "Description", example = "5 000 SMS/mois, support email")
        String description,

        @Schema(description = "Limite mensuelle de SMS, null si illimité", example = "5000")
        Integer monthlySmsLimit,

        @Schema(description = "true si le pack est illimité (monthlySmsLimit = null)", example = "false")
        boolean unlimited,

        @Schema(description = "Prix mensuel indicatif", example = "15000")
        BigDecimal priceMonthly,

        @Schema(description = "Pack disponible à l'assignation. false = retiré de la vente, mais les " +
                "clients qui l'ont déjà gardent leur quota.", example = "true")
        boolean active
) {}