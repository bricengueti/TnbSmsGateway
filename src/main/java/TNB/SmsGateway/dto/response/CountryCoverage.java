package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Couverture d'un pays")
public record CountryCoverage(
        @Schema(description = "Code pays", example = "CM")
        String countryCode,

        @Schema(description = "Nom du pays", example = "Cameroun")
        String countryName,

        @Schema(description = "Liste des opérateurs disponibles")
        List<OperatorCoverage> operators
) {}

