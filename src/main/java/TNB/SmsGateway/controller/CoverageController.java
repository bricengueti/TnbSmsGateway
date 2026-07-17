package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.response.CoverageResponse;
import TNB.SmsGateway.service.CoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * CONTROLLER: CoverageController
 *
 * DESCRIPTION: Fournit la couverture disponible par pays et opérateur
 * URL de base: /api/v1/coverage (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. Le client consulte sa couverture avant d'envoyer un SMS
 * 2. Le client vérifie les dispositifs disponibles par pays/opérateur
 */
@RestController
@RequestMapping("/v1/coverage")
@SecurityRequirement(name = "BearerAuth")
@SecurityRequirement(name = "ApiKeyAuth")
@Tag(name = "Couverture", description = "Couverture disponible par pays et opérateur")
public class CoverageController {

    private final CoverageService coverageService;

    public CoverageController(CoverageService coverageService) {
        this.coverageService = coverageService;
    }

    /**
     * SCÉNARIO: Le client consulte sa couverture
     *
     * URL: GET /api/v1/coverage
     * Auth: JWT ou ApiKey
     *
     * RÉPONSES:
     * - 200: Couverture détaillée
     * - 401: Token invalide
     */
    @Operation(
            summary = "Consulter la couverture",
            description = "Retourne le nombre de devices et SIMs disponibles par pays et opérateur."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Couverture récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = CoverageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token invalide"
            )
    })
    @GetMapping
    public ResponseEntity<CoverageResponse> getCoverage(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        CoverageResponse response = coverageService.getCoverage(userId);
        return ResponseEntity.ok(response);
    }
}