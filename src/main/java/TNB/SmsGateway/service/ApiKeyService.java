package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.ApiKeyRequest;
import TNB.SmsGateway.dto.response.ApiKeyResponse;
import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.ApiKeyRepository;
import TNB.SmsGateway.utils.ApiKeyUtils;
import TNB.SmsGateway.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SERVICE: ApiKeyService
 *
 * DESCRIPTION: Gère les clés API pour l'intégration technique
 * - Création de clés avec scopes (FULL/SEND_ONLY/READ_ONLY)
 * - Liste des clés (préfixe uniquement)
 * - Révocation immédiate
 * - Validation des clés et scopes
 *
 * SCÉNARIOS:
 * 1. Création: l'utilisateur génère une clé pour son application
 * 2. Affichage: la clé complète n'est visible qu'une fois
 * 3. Révocation: clé compromise → blocage immédiat
 * 4. Validation: filtres vérifient format + hash + scope
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final UserService userService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, UserService userService) {
        this.apiKeyRepository = apiKeyRepository;
        this.userService = userService;
    }

    /**
     * SCÉNARIO: Créer une nouvelle clé API
     * ÉTAPES:
     * 1. Vérifier l'utilisateur
     * 2. Générer une clé au format "tnb_live_xxx"
     * 3. Hacher la clé pour stockage
     * 4. Extraire le préfixe pour l'affichage
     * 5. Sauvegarder avec le scope et le label
     * 6. Retourner la clé complète (UNE SEULE FOIS)
     *
     * @param userId ID de l'utilisateur
     * @param request Scope et label
     * @return ApiKeyResponse avec la clé complète
     */
    @Transactional
    public ApiKeyResponse createApiKey(UUID userId, ApiKeyRequest request) {
        User user = userService.findByIdOrThrow(userId);

        String rawApiKey = ApiKeyUtils.generateApiKey();
        String hashedKey = SecurityUtils.hash(rawApiKey);
        String prefix = ApiKeyUtils.extractPrefix(rawApiKey);

        // 🔥 LOG POUR VOIR CE QUI EST SAUVEGARDÉ
        log.info("🔑 Création d'une clé API");
        log.info("   - raw: {}", rawApiKey);
        log.info("   - hash: {}", hashedKey);
        log.info("   - prefix: {}", prefix);

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setKeyHash(hashedKey);
        apiKey.setKeyPrefix(prefix);
        apiKey.setScope(ApiKeyScope.valueOf(request.scope()));
        apiKey.setLabel(request.label());

        ApiKey saved = apiKeyRepository.save(apiKey);

        // 🔥 VÉRIFIER QUE L'ID N'EST PAS NULL
        log.info("✅ Clé API enregistrée avec id: {}", saved.getId());
        log.info("✅ Hash en base: {}", saved.getKeyHash());

        return new ApiKeyResponse(
                saved.getId().toString(),
                rawApiKey,
                prefix,
                saved.getScope().name(),
                saved.getLabel(),
                saved.getCreatedAt()
        );
    }
    /**
     * SCÉNARIO: Lister toutes les clés d'un utilisateur
     * La clé complète n'est plus visible → seulement le préfixe
     *
     * @param userId ID de l'utilisateur
     * @return Liste des clés (sans la clé complète)
     */
    public List<ApiKeyResponse> listApiKeys(UUID userId) {
        List<ApiKey> keys = apiKeyRepository.findByUserId(userId);

        return keys.stream()
                .map(key -> new ApiKeyResponse(
                        key.getId().toString(),
                        null, // La clé complète n'est plus visible
                        key.getKeyPrefix(),
                        key.getScope().name(),
                        key.getLabel(),
                        key.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * SCÉNARIO: Révoquer une clé API (immédiat)
     * ÉTAPES:
     * 1. Vérifier que la clé existe
     * 2. Vérifier que l'utilisateur est propriétaire
     * 3. Marquer comme révoquée (revokedAt = now)
     *
     * @param userId ID de l'utilisateur
     * @param keyId ID de la clé à révoquer
     * @throws BusinessException Si clé non trouvée ou accès non autorisé
     */
    @Transactional
    public void revokeApiKey(UUID userId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new BusinessException("Clé API non trouvée", "API_KEY_NOT_FOUND", 404));

        if (!apiKey.getUser().getId().equals(userId)) {
            throw new BusinessException("Accès non autorisé", "FORBIDDEN", 403);
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
    }

    /**
     * SCÉNARIO: Valider une clé API (utilisé par les filtres)
     * ÉTAPES:
     * 1. Hacher la clé reçue
     * 2. Rechercher en base
     * 3. Vérifier qu'elle n'est pas révoquée
     *
     * @param rawApiKey Clé API en clair
     * @return ApiKey si valide
     * @throws BusinessException Si clé invalide ou révoquée
     */
    public ApiKey validateApiKey(String rawApiKey) {
        String hashedKey = SecurityUtils.hash(rawApiKey);

        ApiKey apiKey = apiKeyRepository.findByKeyHash(hashedKey)
                .orElseThrow(() -> new BusinessException("Clé API invalide", "INVALID_API_KEY", 401));

        if (apiKey.isRevoked()) {
            throw new BusinessException("Clé API révoquée", "API_KEY_REVOKED", 401);
        }

        return apiKey;
    }

    /**
     * SCÉNARIO: Vérifier le scope d'une clé API
     * - FULL: tout est autorisé
     * - SEND_ONLY: seulement POST /messages/send
     * - READ_ONLY: seulement GET
     *
     * @param apiKey Entité ApiKey
     * @param path Chemin de la requête
     * @param method Méthode HTTP
     * @return true si le scope autorise l'action
     */
    public boolean hasScope(ApiKey apiKey, String path, String method) {
        ApiKeyScope scope = apiKey.getScope();

        if (scope == ApiKeyScope.FULL) {
            return true;
        }

        if (scope == ApiKeyScope.SEND_ONLY) {
            return path.contains("/messages/send") && "POST".equalsIgnoreCase(method);
        }

        if (scope == ApiKeyScope.READ_ONLY) {
            return "GET".equalsIgnoreCase(method);
        }

        return false;
    }
}