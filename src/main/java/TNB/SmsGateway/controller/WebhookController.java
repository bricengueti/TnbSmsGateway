package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.WebhookRequest;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.UserService;
import TNB.SmsGateway.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/webhook")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Webhook", description = "Configuration des webhooks pour les SMS entrants")
public class WebhookController {

    private final UserService userService;
    private final WebhookService webhookService;

    public WebhookController(UserService userService, WebhookService webhookService) {
        this.userService = userService;
        this.webhookService = webhookService;
    }

    @Operation(
            summary = "Consulter la configuration du webhook",
            description = "Retourne l'URL configurée et le secret (masqué)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Configuration récupérée"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWebhookConfig(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        return ResponseEntity.ok(Map.of(
                "webhookUrl", user.getWebhookUrl(),
                "webhookSecret", user.getWebhookSecret() != null ? "••••••••" : null,
                "hasWebhook", user.getWebhookUrl() != null
        ));
    }

    @Operation(
            summary = "Configurer l'URL du webhook",
            description = "Définit l'URL où les SMS entrants seront envoyés. L'URL doit commencer par http:// ou https://"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "URL configurée avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "URL invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @PutMapping
    public ResponseEntity<ApiResponse> updateWebhookUrl(
            @Valid @RequestBody WebhookRequest request,
            Authentication authentication
    ) {
        UUID userId = getCurrentUserId(authentication);
        userService.updateWebhookUrl(userId, request.webhookUrl());

        return ResponseEntity.ok(new ApiResponse("Webhook URL mise à jour avec succès", true));
    }

    @Operation(
            summary = "Tester le webhook",
            description = "Envoie un payload de test au webhook configuré. La requête est signée avec le secret."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Test réussi"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Webhook non configuré"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Test échoué"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        if (user.getWebhookUrl() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Webhook non configuré"
            ));
        }

        boolean success = webhookService.testWebhook(user);

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Test réussi" : "Test échoué"
        ));
    }

    @Operation(
            summary = "Roter le secret du webhook",
            description = "Génère un nouveau secret. L'ancien secret est invalidé immédiatement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Secret roté avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token JWT invalide ou manquant"
            )
    })
    @PostMapping("/secret/rotate")
    public ResponseEntity<Map<String, String>> rotateWebhookSecret(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.rotateWebhookSecret(userId);

        return ResponseEntity.ok(Map.of(
                "message", "Secret roté avec succès",
                "newSecret", user.getWebhookSecret()
        ));
    }

    private UUID getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new RuntimeException("Impossible d'extraire l'ID utilisateur");
    }
}