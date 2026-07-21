package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.ApiKey;
import TNB.SmsGateway.entity.ApiKeyScope;
import TNB.SmsGateway.entity.PairingCode;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.ApiKeyRepository;
import TNB.SmsGateway.repository.PairingCodeRepository;
import TNB.SmsGateway.utils.ApiKeyUtils;
import TNB.SmsGateway.utils.RandomUtils;
import TNB.SmsGateway.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationCredentialsService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationCredentialsService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final PairingCodeRepository pairingCodeRepository;

    public IntegrationCredentialsService(ApiKeyRepository apiKeyRepository,
                                         PairingCodeRepository pairingCodeRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.pairingCodeRepository = pairingCodeRepository;
    }

    public record ActivationResult(
            String apiKeyId,
            String apiKey,
            String apiKeyPrefix,
            String pairingCode
    ) {}

    /**
     * SCÉNARIO: Activer l'intégration (dashboard, une seule fois par compte)
     * ÉTAPES:
     * 1. Vérifier qu'aucune intégration n'est déjà active (un seul code de
     *    connexion actif par compte à la fois) — sinon on bloque et on
     *    redirige l'utilisateur vers "Régénérer le code" à la place.
     * 2. Générer et sauvegarder la clé API
     * 3. Générer et sauvegarder le code de connexion
     * 4. Retourner les deux valeurs en clair (dernière fois qu'elles seront visibles)
     */
    @Transactional
    public ActivationResult activateIntegration(User user, String apiKeyLabel) {
        // ✅ Ajouté : bloque la double activation
        pairingCodeRepository.findActiveByUserId(user.getId()).ifPresent(existing -> {
            throw new BusinessException(
                    "Une intégration est déjà active pour ce compte. Utilisez la régénération du code " +
                            "de connexion si besoin d'un nouveau code.",
                    "INTEGRATION_ALREADY_ACTIVATED",
                    409
            );
        });

        String rawApiKey = ApiKeyUtils.generateApiKey();
        String apiKeyHash = SecurityUtils.hash(rawApiKey);
        String apiKeyPrefix = ApiKeyUtils.extractPrefix(rawApiKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setKeyHash(apiKeyHash);
        apiKey.setKeyPrefix(apiKeyPrefix);
        apiKey.setScope(ApiKeyScope.FULL);
        apiKey.setLabel(apiKeyLabel != null && !apiKeyLabel.isBlank() ? apiKeyLabel : "Intégration principale");
        ApiKey savedApiKey = apiKeyRepository.save(apiKey);

        String rawPairingCode = RandomUtils.generatePairingCode();
        // Dans IntegrationCredentialsService.activateIntegration() et regeneratePairingCode() :
        String pairingCodeHash = SecurityUtils.hashSha256(rawPairingCode);
        String pairingCodePrefix = rawPairingCode.substring(0, Math.min(3, rawPairingCode.length()));

        PairingCode pairingCode = new PairingCode(user, pairingCodeHash, pairingCodePrefix);
        pairingCodeRepository.save(pairingCode);

        log.info("✅ Intégration activée pour user {} — apiKeyId={}, pairingCodePrefix={}",
                user.getId(), savedApiKey.getId(), pairingCodePrefix);

        return new ActivationResult(
                savedApiKey.getId().toString(),
                rawApiKey,
                apiKeyPrefix,
                rawPairingCode
        );
    }

    /**
     * SCÉNARIO: Régénérer le code de connexion (ex: fuite suspectée)
     * N'affecte pas la clé API ni les devices déjà pairés.
     */
    @Transactional
    public String regeneratePairingCode(User user) {
        pairingCodeRepository.findActiveByUserId(user.getId())
                .ifPresent(existing -> {
                    existing.revoke();
                    pairingCodeRepository.save(existing);
                });

        String rawPairingCode = RandomUtils.generatePairingCode();
        // Dans IntegrationCredentialsService.activateIntegration() et regeneratePairingCode() :
        String pairingCodeHash = SecurityUtils.hashSha256(rawPairingCode);
        String pairingCodePrefix = rawPairingCode.substring(0, Math.min(3, rawPairingCode.length()));

        PairingCode pairingCode = new PairingCode(user, pairingCodeHash, pairingCodePrefix);
        pairingCodeRepository.save(pairingCode);

        log.info("🔄 Code de connexion régénéré pour user {}", user.getId());
        return rawPairingCode;
    }
}