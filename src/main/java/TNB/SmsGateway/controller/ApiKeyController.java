package TNB.SmsGateway.controller;
import TNB.SmsGateway.dto.request.ApiKeyRequest;
import TNB.SmsGateway.dto.response.ApiKeyResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CONTROLLER: ApiKeyController
 *
 * DESCRIPTION: Gère les clés API pour l'intégration technique
 * URL de base: /api/v1/api-keys (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. L'utilisateur crée une clé API avec un scope
 * 2. L'utilisateur liste ses clés (préfixe uniquement)
 * 3. L'utilisateur révoque une clé (immédiat)
 */
@RestController
@RequestMapping("/v1/api-keys")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Clés API", description = "Gestion des clés API pour l'intégration technique")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * SCÉNARIO: L'utilisateur crée une clé API
     *
     * URL: POST /api/v1/api-keys
     *
     * RÉPONSES:
     * - 201: Clé créée (affichée une seule fois)
     * - 400: Scope invalide
     * - 401: Token invalide
     */
    @Operation(
            summary = "Créer une clé API",
            description = "Génère une nouvelle clé API. La clé complète n'est affichée qu'une seule fois. Scopes disponibles: FULL, SEND_ONLY, READ_ONLY"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Clé créée avec succès",
                    content = @Content(schema = @Schema(implementation = ApiKeyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Scope invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @Valid @RequestBody ApiKeyRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        ApiKeyResponse response = apiKeyService.createApiKey(userId, request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * SCÉNARIO: L'utilisateur liste ses clés
     *
     * URL: GET /api/v1/api-keys
     *
     * RÉPONSES:
     * - 200: Liste des clés (préfixe uniquement)
     * - 401: Token invalide
     */
    @Operation(
            summary = "Lister les clés API",
            description = "Retourne la liste des clés API. La clé complète n'est pas visible, seulement le préfixe."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(userId);
        return ResponseEntity.ok(keys);
    }

    /**
     * SCÉNARIO: L'utilisateur révoque une clé
     *
     * URL: DELETE /api/v1/api-keys/{id}
     *
     * RÉPONSES:
     * - 200: Clé révoquée
     * - 404: Clé non trouvée
     * - 403: Accès non autorisé
     * - 401: Token invalide
     */
    @Operation(
            summary = "Révoquer une clé API",
            description = "Révoque immédiatement une clé API. La clé ne peut plus être utilisée après révocation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Clé révoquée avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Clé non trouvée"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> revokeApiKey(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        apiKeyService.revokeApiKey(userId, id);
        return ResponseEntity.ok(new ApiResponse("Clé API révoquée avec succès", true));
    }
}