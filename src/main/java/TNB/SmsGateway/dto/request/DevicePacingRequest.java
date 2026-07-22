package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de réglage de la cadence d'envoi par défaut d'un device")
public record DevicePacingRequest(
        @Schema(description = "Délai minimum en secondes entre 2 SMS", example = "8")
        @NotNull(message = "Le délai minimum est obligatoire")
        @Min(value = 1, message = "Le délai minimum doit être d'au moins 1 seconde")
        Integer minDelaySec,

        @Schema(description = "Délai maximum en secondes entre 2 SMS", example = "25")
        @NotNull(message = "Le délai maximum est obligatoire")
        @Min(value = 1, message = "Le délai maximum doit être d'au moins 1 seconde")
        Integer maxDelaySec
) {}