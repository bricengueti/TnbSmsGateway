package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Requête d'assignation d'un pack POOL (crédits SMS mensuels) à un utilisateur")
public record AssignPoolPlanRequest(
        @Schema(description = "ID du pack POOL à assigner — remplace toute limite précédente, ne " +
                "s'additionne pas", example = "550e8400-...")
        @NotNull(message = "L'ID du pack est obligatoire")
        UUID planId
) {}