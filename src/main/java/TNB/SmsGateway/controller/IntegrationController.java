package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.dto.request.IntegrationActivateRequest;
import TNB.SmsGateway.dto.response.IntegrationActivationResponse;
import TNB.SmsGateway.dto.response.PairingCodeResponse;
import TNB.SmsGateway.entity.DeviceType;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.security.UserPrincipal;
import TNB.SmsGateway.service.IntegrationCredentialsService;
import TNB.SmsGateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "Génère un code de connexion (et optionnellement une clé API) en une seule action. " +
                    "targetType=PERSONAL : pour appairer vos propres téléphones-passerelles (usage classique). " +
                    "targetType=POOL : pour contribuer un ou plusieurs téléphones au pool partagé de la " +
                    "plateforme (marketplace) — dans ce cas, withApiKey peut être mis à false si vous ne " +
                    "comptez pas envoyer de SMS vous-même. Les deux types de code peuvent coexister sur un " +
                    "même compte."
    )
    @PostMapping("/activate")
    public ResponseEntity<IntegrationActivationResponse> activate(
            @Valid @RequestBody IntegrationActivateRequest request,
            Authentication authentication
    ) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        IntegrationCredentialsService.ActivationResult result =
                integrationCredentialsService.activateIntegration(
                        user, request.apiKeyLabel(),
                        request.resolvedTargetType(), request.resolvedWithApiKey()
                );

        return ResponseEntity.status(201).body(new IntegrationActivationResponse(
                result.apiKeyId(), result.apiKey(), result.apiKeyPrefix(),
                result.pairingCode(), result.targetType().name()
        ));
    }


    @Operation(
            summary = "Régénérer le code de connexion",
            description = "Invalide l'ancien code de connexion actif pour ce type de device et en génère " +
                    "un nouveau. N'affecte pas la clé API ni les devices déjà pairés. Si le compte a un " +
                    "code PERSONAL et un code POOL actifs en parallèle, seul celui du type demandé est régénéré."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Nouveau code généré"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Token JWT invalide ou manquant")
    })
    @PostMapping("/pairing-code/regenerate")
    public ResponseEntity<PairingCodeResponse> regeneratePairingCode(
            @Parameter(description = "Type de device visé par le code à régénérer. PERSONAL par défaut " +
                    "pour compatibilité avec les intégrations existantes qui n'envoient pas ce paramètre.",
                    example = "POOL")
            @RequestParam(defaultValue = "PERSONAL") DeviceType targetType,
            Authentication authentication
    ) {
        UUID userId = getCurrentUserId(authentication);
        User user = userService.findByIdOrThrow(userId);

        String newCode = integrationCredentialsService.regeneratePairingCode(user, targetType);
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