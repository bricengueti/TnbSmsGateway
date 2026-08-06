package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Changement de statut d'un compte utilisateur (suspension/réactivation)")
public record UpdateUserStatusRequest(
        @Schema(description = "Nouveau statut du compte", allowableValues = {"ACTIVE", "SUSPENDED"}, example = "SUSPENDED")
        @NotNull(message = "Le statut est obligatoire")
        String status
) {}