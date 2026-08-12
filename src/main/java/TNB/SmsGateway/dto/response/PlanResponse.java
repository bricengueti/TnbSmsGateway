package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Détails d'un pack tarifaire")
public record PlanResponse(
        String id,
        String name,
        String description,

        @Schema(allowableValues = {"POOL", "PERSONAL"})
        String type,

        @Schema(description = "Durée de validité du pack en mois")
        Integer validityMonths,

        @Schema(description = "Quota total de SMS pour la période (POOL), null si illimité ou si type=PERSONAL")
        Integer quantitySms,

        @Schema(description = "Plafond de devices simultanés (PERSONAL), null si illimité ou si type=POOL")
        Integer quantityDevices,

        BigDecimal price,
        boolean active
) {}
