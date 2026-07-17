package TNB.SmsGateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * CONTROLLER: HealthController
 *
 * DESCRIPTION: Health check pour le monitoring
 * URL: /api/v1/health (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. Load balancer vérifie si l'application est en vie
 * 2. Monitoring vérifie l'état du service
 */
@RestController
@RequestMapping("/v1/health")
@Tag(name = "Health", description = "Health check pour le monitoring")
public class HealthController {

    /**
     * SCÉNARIO: Vérification de l'état du service
     *
     * URL: GET /api/v1/health
     * Auth: Aucune
     *
     * RÉPONSES:
     * - 200: Service en vie
     */
    @Operation(
            summary = "Health check",
            description = "Vérifie que le service est en vie. Utilisé par les load balancers et les outils de monitoring."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Service en vie",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "service", "TNB SMS Gateway"
        ));
    }
}