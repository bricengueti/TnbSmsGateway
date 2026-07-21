package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.dto.request.IntegrationActivateRequest;
import TNB.SmsGateway.dto.response.IntegrationActivationResponse;
import TNB.SmsGateway.dto.response.PairingCodeResponse;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.IntegrationCredentialsService;
import TNB.SmsGateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/integration")
@Tag(name = "Intégration", description = "Activation de l'intégration : clé API + code de connexion générés ensemble")
@SecurityRequirement(name = "BearerAuth")
public class IntegrationController {

    private final IntegrationCredentialsService integrationCredentialsService;
    private final UserService userService;

    public IntegrationController(IntegrationCredentialsService integrationCredentialsService,
                                 UserService userService) {
        this.integrationCredentialsService = integrationCredentialsService;
        this.userService = userService;
    }

    @Operation(
            summary = "Activer l'intégration",
            description = "Génère en une seule action la clé API (pour l'application externe de " +
                    "l'utilisateur) et le code de connexion (à saisir sur chaque téléphone-passerelle). " +
                    "Les deux valeurs complètes ne sont affichées qu'une seule fois."
    )
    @PostMapping("/activate")
    public ResponseEntity<IntegrationActivationResponse> activate(
            @Valid @RequestBody IntegrationActivateRequest request,
            Authentication authentication
    ) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        IntegrationCredentialsService.ActivationResult result =
                integrationCredentialsService.activateIntegration(user, request.apiKeyLabel());

        return ResponseEntity.status(201).body(new IntegrationActivationResponse(
                result.apiKeyId(),
                result.apiKey(),
                result.apiKeyPrefix(),
                result.pairingCode()
        ));
    }

    @Operation(
            summary = "Régénérer le code de connexion",
            description = "Invalide l'ancien code de connexion et en génère un nouveau. N'affecte pas la " +
                    "clé API ni les devices déjà pairés."
    )
    @PostMapping("/pairing-code/regenerate")
    public ResponseEntity<PairingCodeResponse> regeneratePairingCode(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        String newCode = integrationCredentialsService.regeneratePairingCode(user);
        return ResponseEntity.ok(new PairingCodeResponse(newCode));
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