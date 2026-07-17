package TNB.SmsGateway.controller;

import TNB.SmsGateway.dto.request.AuthRequest;
import TNB.SmsGateway.dto.request.OtpRequest;
import TNB.SmsGateway.dto.request.RefreshRequest;
import TNB.SmsGateway.dto.response.AuthResponse;
import TNB.SmsGateway.dto.response.OtpResponse;
import TNB.SmsGateway.dto.response.RefreshResponse;
import TNB.SmsGateway.dto.common.ApiResponse;
import TNB.SmsGateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: AuthController
 *
 * DESCRIPTION: Gère l'authentification des utilisateurs
 * URL de base: /api/v1/auth (context-path=/api)
 *
 * SCÉNARIOS:
 * 1. L'utilisateur demande un OTP → reçoit un code par email
 * 2. L'utilisateur soumet l'OTP → reçoit accessToken + refreshToken
 * 3. L'access token expire → refresh token pour un nouveau
 * 4. L'utilisateur se déconnecte → suppression des tokens côté client
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentification", description = "Gestion de l'authentification OTP et des tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =============================================
    // ===== DEMANDE OTP =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur demande un OTP
     *
     * URL: POST /api/v1/auth/otp/request
     *
     * RÉPONSES:
     * - 200: OTP envoyé avec succès
     * - 429: Trop de tentatives
     */
    @Operation(
            summary = "Demander un OTP",
            description = "Envoie un code OTP à 6 chiffres par email. Limite: 3 requêtes / 10 minutes"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OTP envoyé avec succès",
                    content = @Content(schema = @Schema(implementation = OtpResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Trop de tentatives. Veuillez patienter 10 minutes"
            )
    })
    @PostMapping("/otp/request")
    public ResponseEntity<OtpResponse> requestOtp(
            @Valid @RequestBody AuthRequest request
    ) {
        authService.requestOtp(request.email());

        return ResponseEntity.ok(new OtpResponse(
                "OTP envoyé avec succès",
                request.email(),
                5
        ));
    }

    // =============================================
    // ===== VÉRIFICATION OTP =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur soumet le code OTP reçu
     *
     * URL: POST /api/v1/auth/otp/verify
     *
     * RÉPONSES:
     * - 200: Authentification réussie
     * - 400: Code invalide ou expiré
     * - 429: Trop de tentatives échouées
     */
    @Operation(
            summary = "Vérifier l'OTP et s'authentifier",
            description = "Vérifie le code OTP et délivre les tokens JWT. Si le compte n'existe pas, il est créé automatiquement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentification réussie",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Code OTP invalide ou expiré"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Trop de tentatives échouées (max 5)"
            )
    })
    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpRequest request
    ) {
        AuthResponse response = authService.verifyOtp(request.email(), request.code());
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== REFRESH TOKEN =====
    // =============================================

    /**
     * SCÉNARIO: L'access token expire, on utilise le refresh token
     *
     * URL: POST /api/v1/auth/refresh
     *
     * RÉPONSES:
     * - 200: Nouvel access token généré
     * - 401: Refresh token invalide ou expiré
     */
    @Operation(
            summary = "Rafraîchir l'access token",
            description = "Utilise le refresh token pour obtenir un nouvel access token. Le refresh token a une validité de 7 jours."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Nouvel access token généré",
                    content = @Content(schema = @Schema(implementation = RefreshResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalide ou expiré"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(
            @Valid @RequestBody RefreshRequest request
    ) {
        RefreshResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ===== DÉCONNEXION =====
    // =============================================

    /**
     * SCÉNARIO: L'utilisateur se déconnecte
     *
     * URL: POST /api/v1/auth/logout
     *
     * RÉPONSES:
     * - 200: Déconnexion réussie
     */
    @Operation(
            summary = "Déconnexion",
            description = "Déconnecte l'utilisateur. Le client doit supprimer les tokens localement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Déconnexion réussie"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        authService.logout();
        return ResponseEntity.ok(new ApiResponse("Déconnecté avec succès", true));
    }
}