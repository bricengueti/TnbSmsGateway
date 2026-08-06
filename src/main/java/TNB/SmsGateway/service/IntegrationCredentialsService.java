package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.*;
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
            String apiKeyId,      // nullable désormais
            String apiKey,        // nullable désormais
            String apiKeyPrefix,  // nullable désormais
            String pairingCode,
            DeviceType targetType
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
    public ActivationResult activateIntegration(User user, String apiKeyLabel,
                                                DeviceType targetType, boolean withApiKey) {

        // ✅ Le check de doublon est maintenant scopé par type : un compte peut
        // avoir un code PERSONAL actif ET un code POOL actif simultanément.
        pairingCodeRepository.findActiveByUserIdAndTargetType(user.getId(), targetType).ifPresent(existing -> {
            throw new BusinessException(
                    "Une intégration " + targetType + " est déjà active pour ce compte. Utilisez la " +
                            "régénération du code de connexion si besoin d'un nouveau code.",
                    "INTEGRATION_ALREADY_ACTIVATED",
                    409
            );
        });

        String apiKeyIdStr = null;
        String rawApiKey = null;
        String apiKeyPrefix = null;

        if (withApiKey) {
            rawApiKey = ApiKeyUtils.generateApiKey();
            String apiKeyHash = SecurityUtils.hash(rawApiKey);
            apiKeyPrefix = ApiKeyUtils.extractPrefix(rawApiKey);

            ApiKey apiKey = new ApiKey();
            apiKey.setUser(user);
            apiKey.setKeyHash(apiKeyHash);
            apiKey.setKeyPrefix(apiKeyPrefix);
            apiKey.setScope(ApiKeyScope.FULL);
            apiKey.setLabel(apiKeyLabel != null && !apiKeyLabel.isBlank() ? apiKeyLabel : "Intégration principale");
            ApiKey savedApiKey = apiKeyRepository.save(apiKey);
            apiKeyIdStr = savedApiKey.getId().toString();
        }

        String rawPairingCode = RandomUtils.generatePairingCode();
        String pairingCodeHash = SecurityUtils.hashSha256(rawPairingCode);
        String pairingCodePrefix = rawPairingCode.substring(0, Math.min(3, rawPairingCode.length()));

        PairingCode pairingCode = new PairingCode(user, pairingCodeHash, pairingCodePrefix);
        pairingCode.setTargetType(targetType);
        pairingCodeRepository.save(pairingCode);

        log.info("✅ Intégration activée pour user {} — type={}, apiKeyId={}, pairingCodePrefix={}",
                user.getId(), targetType, apiKeyIdStr, pairingCodePrefix);

        return new ActivationResult(apiKeyIdStr, rawApiKey, apiKeyPrefix, rawPairingCode, targetType);
    }
    /**
     * SCÉNARIO: Régénérer le code de connexion (ex: fuite suspectée)
     * N'affecte pas la clé API ni les devices déjà pairés.
     */
    @Transactional
    public String regeneratePairingCode(User user, DeviceType targetType) {
        pairingCodeRepository.findActiveByUserIdAndTargetType(user.getId(), targetType)
                .ifPresent(existing -> {
                    existing.revoke();
                    pairingCodeRepository.save(existing);
                });

        String rawPairingCode = RandomUtils.generatePairingCode();
        String pairingCodeHash = SecurityUtils.hashSha256(rawPairingCode);
        String pairingCodePrefix = rawPairingCode.substring(0, Math.min(3, rawPairingCode.length()));

        PairingCode pairingCode = new PairingCode(user, pairingCodeHash, pairingCodePrefix);
        pairingCode.setTargetType(targetType);
        pairingCodeRepository.save(pairingCode);

        log.info("🔄 Code de connexion régénéré pour user {} (type={})", user.getId(), targetType);
        return rawPairingCode;
    }
}