package TNB.SmsGateway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Requête de surcharge de cadence pour une SIM précise (null = retirer la surcharge)")
public record SimPacingRequest(
        @Schema(description = "Délai minimum en secondes (null pour retirer la surcharge)", example = "8")
        @Min(value = 1, message = "Le délai minimum doit être d'au moins 1 seconde")
        Integer minDelaySec,

        @Schema(description = "Délai maximum en secondes (null pour retirer la surcharge)", example = "25")
        @Min(value = 1, message = "Le délai maximum doit être d'au moins 1 seconde")
        Integer maxDelaySec
) {}