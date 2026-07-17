package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.response.AuthResponse;
import TNB.SmsGateway.dto.response.RefreshResponse;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.entity.UserStatus;
import TNB.SmsGateway.exception.authentication.InvalidOtpException;
import TNB.SmsGateway.exception.authentication.InvalidTokenException;
import TNB.SmsGateway.exception.authentication.OtpExpiredException;
import TNB.SmsGateway.exception.authentication.OtpRateLimitException;
import TNB.SmsGateway.utils.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * SERVICE: AuthService
 *
 * DESCRIPTION: Gère l'authentification complète des utilisateurs
 * - Demande d'OTP par email
 * - Vérification OTP et création de session
 * - Génération et rafraîchissement des tokens JWT
 * - Déconnexion (stateless)
 *
 * SCÉNARIOS:
 * 1. Nouvel utilisateur: email inconnu → création automatique du compte
 * 2. Utilisateur existant: email connu → login avec OTP
 * 3. Token expiré: refresh token → nouvel access token
 * 4. Compte suspendu: blocage de l'authentification
 */
@Service
public class AuthService {

    private final OtpService otpService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthService(OtpService otpService, UserService userService, JwtUtils jwtUtils) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * SCÉNARIO: L'utilisateur saisit son email pour recevoir un OTP
     * ÉTAPES:
     * 1. Vérifier le rate limit (max 3 requêtes/10min)
     * 2. Générer un OTP à 6 chiffres
     * 3. Hacher le code pour stockage
     * 4. Envoyer par email
     * 5. Stocker avec expiration +5min
     *
     * @param email Email de l'utilisateur
     * @throws OtpRateLimitException Si trop de tentatives
     */
    @Transactional
    public void requestOtp(String email) {
        // Vérifier rate limit
        if (otpService.isRateLimitExceeded(email)) {
            throw new OtpRateLimitException();
        }
        // Générer et envoyer l'OTP
        otpService.generateAndSendOtp(email);
    }

    /**
     * SCÉNARIO: L'utilisateur soumet le code OTP reçu par email
     * ÉTAPES:
     * 1. Vérifier que l'OTP existe et n'est pas expiré
     * 2. Vérifier le code (comparaison hash)
     * 3. Incrémenter les tentatives si erreur
     * 4. Marquer l'OTP comme vérifié
     * 5. Créer ou récupérer l'utilisateur
     * 6. Vérifier que le compte n'est pas suspendu
     * 7. Générer accessToken (12h) et refreshToken (7 jours)
     * 8. Retourner les tokens et les infos utilisateur
     *
     * @param email Email de l'utilisateur
     * @param code Code OTP à 6 chiffres
     * @return AuthResponse avec tokens et infos utilisateur
     * @throws InvalidOtpException Si code invalide
     * @throws OtpExpiredException Si code expiré
     * @throws OtpRateLimitException Si 5 tentatives échouées
     */
    @Transactional
    public AuthResponse verifyOtp(String email, String code) {
        // 1. Vérifier l'OTP
        var otpCode = otpService.findValidOtp(email)
                .orElseThrow(InvalidOtpException::new);

        if (otpCode.isExpired()) {
            throw new OtpExpiredException();
        }

        if (!otpService.verifyCode(otpCode, code)) {
            otpService.incrementAttempts(otpCode);
            if (otpCode.getAttempts() >= 5) {
                throw new OtpRateLimitException();
            }
            throw new InvalidOtpException();
        }

        // 2. Marquer l'OTP comme vérifié
        otpService.markAsVerified(otpCode);

        // 3. Récupérer ou créer l'utilisateur
        User user = userService.findByEmail(email)
                .orElseGet(() -> userService.createUser(email));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Compte suspendu");
        }

        // 4. Générer les tokens
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());

        // 5. Vérifier si c'est un nouveau compte
        boolean isNewAccount = user.getCreatedAt().equals(user.getUpdatedAt());

        return new AuthResponse(accessToken, refreshToken, isNewAccount);
    }

    /**
     * SCÉNARIO: L'access token expire, on utilise le refresh token
     * ÉTAPES:
     * 1. Vérifier la signature et l'expiration du refresh token
     * 2. Vérifier que c'est bien un refresh token (type=REFRESH)
     * 3. Extraire userId et email
     * 4. Vérifier que l'utilisateur existe et n'est pas suspendu
     * 5. Générer un nouvel access token
     * 6. Retourner le nouvel access token
     *
     * @param refreshToken Refresh token JWT
     * @return RefreshResponse avec nouvel access token
     * @throws InvalidTokenException Si token invalide ou expiré
     */
    public RefreshResponse refreshToken(String refreshToken) {
        if (!jwtUtils.isValidRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Refresh token invalide ou expiré");
        }

        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Token invalide: n'est pas un refresh token");
        }

        UUID userId = jwtUtils.getUserIdFromToken(refreshToken);
        String email = jwtUtils.getEmailFromToken(refreshToken);

        User user = userService.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Utilisateur non trouvé"));

        if (!user.getEmail().equals(email)) {
            throw new InvalidTokenException("Email ne correspond pas");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidTokenException("Compte suspendu");
        }

        String newAccessToken = jwtUtils.generateAccessToken(userId, email);
        return new RefreshResponse(newAccessToken);
    }

    /**
     * SCÉNARIO: L'utilisateur se déconnecte
     * Rien à faire côté serveur (stateless) → le client supprime ses tokens
     */
    public void logout() {
        // Rien à faire - client supprime ses tokens
    }
}