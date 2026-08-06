package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête d'assignation d'un pack PERSONAL (plafond de devices) à un utilisateur")
public record AssignPersonalPlanRequest(
        @Schema(description = "ID du pack PERSONAL à assigner", example = "550e8400-...")
        @NotNull(message = "L'ID du pack est obligatoire")
        java.util.UUID planId
) {}