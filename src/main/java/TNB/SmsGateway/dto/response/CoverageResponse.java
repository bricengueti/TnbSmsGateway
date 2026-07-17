package TNB.SmsGateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Réponse de couverture par pays et opérateur")
public record CoverageResponse(
        @Schema(description = "Liste des couvertures par pays")
        List<CountryCoverage> countries
) {}
