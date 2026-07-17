package TNB.SmsGateway.controller;

import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Operator;
import TNB.SmsGateway.service.ReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER: ReferenceController
 *
 * DESCRIPTION: Fournit les données de référence (pays, opérateurs)
 * URL de base: /api/v1/reference (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. Le client consulte la liste des pays
 * 2. Le client consulte les opérateurs d'un pays
 * 3. Le client valide un pays/opérateur avant l'envoi
 */
@RestController
@RequestMapping("/v1/reference")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Référentiel", description = "Données de référence: pays et opérateurs")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    /**
     * SCÉNARIO: Le client consulte la liste des pays
     *
     * URL: GET /api/v1/reference/countries
     */
    @Operation(
            summary = "Liste des pays",
            description = "Retourne la liste de tous les pays disponibles avec leurs opérateurs"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès"
            )
    })
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        List<Country> countries = referenceService.findAllCountriesWithOperators();
        return ResponseEntity.ok(countries);
    }

    /**
     * SCÉNARIO: Le client consulte les opérateurs d'un pays
     *
     * URL: GET /api/v1/reference/countries/{code}/operators
     */
    @Operation(
            summary = "Opérateurs d'un pays",
            description = "Retourne la liste des opérateurs pour un pays donné"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Pays non trouvé"
            )
    })
    @GetMapping("/countries/{code}/operators")
    public ResponseEntity<List<Operator>> getOperatorsByCountry(@PathVariable String code) {
        List<Operator> operators = referenceService.findOperatorsByCountry(code);
        return ResponseEntity.ok(operators);
    }

    /**
     * SCÉNARIO: Le client valide un pays
     *
     * URL: GET /api/v1/reference/countries/{code}/validate
     */
    @Operation(
            summary = "Valider un pays",
            description = "Vérifie si un pays existe"
    )
    @GetMapping("/countries/{code}/validate")
    public ResponseEntity<Map<String, Boolean>> validateCountry(@PathVariable String code) {
        boolean exists = referenceService.countryExists(code);
        return ResponseEntity.ok(Map.of("valid", exists));
    }

    /**
     * SCÉNARIO: Le client valide un opérateur
     *
     * URL: GET /api/v1/reference/operators/{code}/validate
     */
    @Operation(
            summary = "Valider un opérateur",
            description = "Vérifie si un opérateur existe"
    )
    @GetMapping("/operators/{code}/validate")
    public ResponseEntity<Map<String, Boolean>> validateOperator(@PathVariable String code) {
        boolean exists = referenceService.findOperatorByCode(code).isPresent();
        return ResponseEntity.ok(Map.of("valid", exists));
    }
}