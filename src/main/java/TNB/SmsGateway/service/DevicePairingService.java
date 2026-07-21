package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.DevicePairRequest;
import TNB.SmsGateway.dto.response.DevicePairResponse;
import TNB.SmsGateway.entity.Country;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.PairingCode;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.exception.device.InvalidPairingCodeException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.PairingCodeRepository;
import TNB.SmsGateway.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * SERVICE: DevicePairingService
 *
 * DESCRIPTION: Gère le pairing entre l'app mobile Android et le compte
 * utilisateur, via le code de connexion réutilisable (PairingCode).
 *
 * SCÉNARIOS:
 * 1. Pairing: l'app mobile soumet le code de connexion + le pays détecté
 *    de la SIM → un NOUVEAU Device est créé à cet instant précis
 * 2. Le même code peut être réutilisé pour pairer plusieurs devices
 * 3. WebSocket: vérification du secret token (BCrypt) à la connexion
 */
@Service
public class DevicePairingService {

    private static final Logger log = LoggerFactory.getLogger(DevicePairingService.class);

    private final DeviceRepository deviceRepository;
    private final PairingCodeRepository pairingCodeRepository;
    private final ReferenceService referenceService;

    public DevicePairingService(DeviceRepository deviceRepository,
                                PairingCodeRepository pairingCodeRepository,
                                ReferenceService referenceService) {
        this.deviceRepository = deviceRepository;
        this.pairingCodeRepository = pairingCodeRepository;
        this.referenceService = referenceService;
    }

    /**
     * SCÉNARIO: L'app mobile soumet le code de connexion pour s'enregistrer
     * ÉTAPES:
     * 1. Hacher le code reçu (déterministe) et le retrouver en base
     * 2. Vérifier qu'il n'est pas révoqué
     * 3. Résoudre le pays depuis le countryIso détecté par l'app Android
     * 4. Créer le Device (status DISABLED, en attente de 1ère connexion WS)
     * 5. Générer le secretToken, le hacher en BCrypt (cohérent avec
     *    DeviceWebSocketHandler.verifySecretToken)
     * 6. Marquer le code de connexion comme utilisé (traçabilité, pas
     *    d'invalidation — il reste réutilisable pour d'autres devices)
     */
    @Transactional
    public DevicePairResponse pairDevice(DevicePairRequest request) {
        String pairingCodeHash = SecurityUtils.hash(request.pairingCode());

        PairingCode pairingCode = pairingCodeRepository.findByCodeHash(pairingCodeHash)
                .orElseThrow(InvalidPairingCodeException::new);

        if (pairingCode.isRevoked()) {
            throw new InvalidPairingCodeException();
        }

        Country country = referenceService.findCountryByCode(request.countryIso())
                .orElseThrow(() -> new BusinessException("Pays non supporté", "COUNTRY_NOT_SUPPORTED", 400));

        String label = (request.deviceLabel() != null && !request.deviceLabel().isBlank())
                ? request.deviceLabel()
                : "Téléphone " + country.getCode();

        Device device = new Device(pairingCode.getUser(), country, label);
        device.setStatus(DeviceStatus.DISABLED); // passera ONLINE à la 1ère connexion WebSocket réussie
        device.setPairedAt(Instant.now());

        // ✅ BCrypt uniformisé (cohérent avec DeviceWebSocketHandler.verifySecretToken)
        String secretToken = UUID.randomUUID().toString().replace("-", "");
        device.setSecretTokenHash(BCrypt.hashpw(secretToken, BCrypt.gensalt()));

        deviceRepository.save(device);

        pairingCode.markUsed();
        pairingCodeRepository.save(pairingCode);

        log.info("✅ Nouveau device {} pairé pour user {} via code {}...",
                device.getId(), pairingCode.getUser().getId(), pairingCode.getCodePrefix());

        return new DevicePairResponse(
                device.getId().toString(),
                secretToken,
                device.getStatus().name()
        );
    }

    /**
     * SCÉNARIO: Vérifier le secret token lors de la connexion WebSocket
     * (méthode conservée pour compatibilité — DeviceWebSocketHandler a
     * actuellement sa propre vérification BCrypt en doublon, à voir si
     * on centralise ici à l'étape suivante)
     */
    public boolean verifySecretToken(String deviceId, String secretToken) {
        try {
            UUID uuid = UUID.fromString(deviceId);
            Device device = deviceRepository.findById(uuid).orElse(null);

            if (device == null || device.getSecretTokenHash() == null || device.isRevoked()) {
                return false;
            }

            return BCrypt.checkpw(secretToken, device.getSecretTokenHash());
        } catch (Exception e) {
            return false;
        }
    }
}