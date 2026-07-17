package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.ApiKeyRequest;
import TNB.SmsGateway.dto.response.ApiKeyResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api-keys")
@Tag(name = "Clés API", description = "Gestion des clés API pour l'intégration technique")
@SecurityRequirement(name = "BearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(
            summary = "Créer une clé API",
            description = "Génère une nouvelle clé API. La clé complète n'est affichée qu'une seule fois.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @Valid @RequestBody ApiKeyRequest request
    ) {
        UUID userId = getCurrentUserId();
        ApiKeyResponse response = apiKeyService.createApiKey(userId, request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(
            summary = "Lister les clés API",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys() {
        UUID userId = getCurrentUserId();
        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(userId);
        return ResponseEntity.ok(keys);
    }

    @Operation(
            summary = "Révoquer une clé API",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> revokeApiKey(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        apiKeyService.revokeApiKey(userId, id);
        return ResponseEntity.ok(new ApiResponse("Clé API révoquée avec succès", true));
    }

    /**
     * Récupérer l'ID de l'utilisateur courant depuis le SecurityContext
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        Object principal = authentication.getPrincipal();

        // Si c'est un UserPrincipal (notre cas avec JWT)
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        // Si c'est directement un UUID (cas ApiKey)
        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new RuntimeException("Impossible d'extraire l'ID utilisateur: " + principal.getClass().getName());
    }
}