package TNB.SmsGateway.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête d'assignation d'un pack à une clé API")
public record AssignPlanRequest(
        @Schema(description = "ID du pack à assigner", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "L'ID du pack est obligatoire")
        java.util.UUID planId
) {}