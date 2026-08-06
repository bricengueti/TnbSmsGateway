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

        @Schema(description = "Limite mensuelle de SMS (POOL), null si illimité ou si type=PERSONAL")
        Integer monthlySmsLimit,

        @Schema(description = "Plafond de devices (PERSONAL), null si illimité ou si type=POOL")
        Integer maxDevices,

        BigDecimal price,
        boolean active
) {}