package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.entity.UserStatus;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * SERVICE: UserService
 *
 * DESCRIPTION: Gère le CRUD des utilisateurs et leurs informations
 * - Création de compte
 * - Recherche par email ou ID
 * - Mise à jour des informations (companyName)
 * - Gestion du status (ACTIVE/SUSPENDED)
 * - Configuration du webhook (URL + secret)
 *
 * SCÉNARIOS:
 * 1. Création: premier login → compte créé automatiquement
 * 2. Consultation: récupération des infos utilisateur
 * 3. Mise à jour: modification du nom d'entreprise
 * 4. Suspension/Réactivation: gestion des comptes
 * 5. Webhook: configuration de l'URL de réception
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * SCÉNARIO: Premier login → création automatique du compte
     * ÉTAPES:
     * 1. Créer un nouvel utilisateur
     * 2. Status ACTIVE
     * 3. Générer un webhook secret aléatoire
     * 4. Sauvegarder
     *
     * @param email Email de l'utilisateur
     * @return Utilisateur créé
     */
    @Transactional
    public User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setWebhookSecret(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    /**
     * SCÉNARIO: Rechercher un utilisateur par email
     *
     * @param email Email de l'utilisateur
     * @return Optional contenant l'utilisateur ou vide
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * SCÉNARIO: Rechercher un utilisateur par ID
     *
     * @param id ID de l'utilisateur
     * @return Optional contenant l'utilisateur ou vide
     */
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * SCÉNARIO: Trouver un utilisateur ou lever une exception
     *
     * @param id ID de l'utilisateur
     * @return Utilisateur trouvé
     * @throws BusinessException Si utilisateur non trouvé
     */
    public User findByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", "USER_NOT_FOUND", 404));
    }

    /**
     * SCÉNARIO: Mettre à jour le nom de l'entreprise
     *
     * @param userId ID de l'utilisateur
     * @param companyName Nouveau nom
     * @return Utilisateur mis à jour
     */
    @Transactional
    public User updateCompanyName(UUID userId, String companyName) {
        User user = findByIdOrThrow(userId);
        user.setCompanyName(companyName);
        return userRepository.save(user);
    }

    /**
     * SCÉNARIO: Configurer l'URL du webhook
     * ÉTAPES:
     * 1. Vérifier que l'utilisateur existe
     * 2. Mettre à jour l'URL
     * 3. Sauvegarder
     *
     * @param userId ID de l'utilisateur
     * @param webhookUrl URL de réception des SMS entrants
     * @return Utilisateur mis à jour
     */
    @Transactional
    public User updateWebhookUrl(UUID userId, String webhookUrl) {
        User user = findByIdOrThrow(userId);
        user.setWebhookUrl(webhookUrl);
        return userRepository.save(user);
    }

    /**
     * SCÉNARIO: Rotation du webhook secret (sécurité)
     * ÉTAPES:
     * 1. Générer un nouveau secret
     * 2. Mettre à jour
     * 3. Sauvegarder
     *
     * @param userId ID de l'utilisateur
     * @return Utilisateur mis à jour
     */
    @Transactional
    public User rotateWebhookSecret(UUID userId) {
        User user = findByIdOrThrow(userId);
        user.setWebhookSecret(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    /**
     * SCÉNARIO: Suspendre un compte utilisateur
     *
     * @param userId ID de l'utilisateur
     */
    @Transactional
    public void suspendUser(UUID userId) {
        User user = findByIdOrThrow(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    /**
     * SCÉNARIO: Réactiver un compte utilisateur
     *
     * @param userId ID de l'utilisateur
     */
    @Transactional
    public void activateUser(UUID userId) {
        User user = findByIdOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * SCÉNARIO: Récupérer le webhook secret
     *
     * @param userId ID de l'utilisateur
     * @return Webhook secret
     */
    public String getWebhookSecret(UUID userId) {
        User user = findByIdOrThrow(userId);
        return user.getWebhookSecret();
    }

    /**
     * SCÉNARIO: Vérifier si un webhook est configuré
     *
     * @param userId ID de l'utilisateur
     * @return true si webhook configuré
     */
    public boolean hasWebhookConfigured(UUID userId) {
        User user = findByIdOrThrow(userId);
        return user.getWebhookUrl() != null && !user.getWebhookUrl().isEmpty();
    }
}