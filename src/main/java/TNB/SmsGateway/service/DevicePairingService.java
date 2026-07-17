package TNB.SmsGateway.service;


import TNB.SmsGateway.dto.request.DevicePairRequest;
import TNB.SmsGateway.dto.response.DevicePairResponse;
import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.exception.device.InvalidPairingCodeException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * SERVICE: DevicePairingService
 *
 * DESCRIPTION: Gère le pairing entre le dashboard et l'app mobile Android
 * - Validation du code de pairing
 * - Génération du secret token
 * - Activation du device
 * - Vérification du secret token pour WebSocket
 *
 * SCÉNARIOS:
 * 1. Pairing: l'app mobile soumet le code de pairing
 * 2. Expiration: code invalide après 15 minutes
 * 3. Re-pairing: impossible si déjà pairé
 * 4. WebSocket: vérification du secret token à la connexion
 */
@Service
public class DevicePairingService {

    private final DeviceRepository deviceRepository;

    public DevicePairingService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * SCÉNARIO: L'app mobile soumet le code de pairing
     * ÉTAPES:
     * 1. Trouver le device par le code de pairing
     * 2. Vérifier que le code n'a pas expiré (15min)
     * 3. Vérifier que le device n'est pas déjà pairé
     * 4. Générer un secret token (UUID sans tirets)
     * 5. Hasher le secret pour stockage
     * 6. Mettre à jour le device (pairedAt, secretTokenHash)
     * 7. Status = OFFLINE (en attente de connexion WS)
     * 8. Retourner le secret token (stocké par l'app mobile)
     *
     * @param request Code de pairing
     * @return DevicePairResponse avec secret token
     * @throws InvalidPairingCodeException Si code invalide/expiré/déjà utilisé
     */
    @Transactional
    public DevicePairResponse pairDevice(DevicePairRequest request) {
        String pairingCode = request.pairingCode();

        // 1. Trouver le device par pairing code
        Device device = deviceRepository.findByPairingCode(pairingCode)
                .orElseThrow(InvalidPairingCodeException::new);

        // 2. Vérifier que le code n'a pas expiré
        if (device.getPairingCodeExpiresAt() == null ||
                device.getPairingCodeExpiresAt().isBefore(Instant.now())) {
            throw new InvalidPairingCodeException();
        }

        // 3. Vérifier que le device n'est pas déjà pairé
        if (device.getPairedAt() != null) {
            throw new InvalidPairingCodeException();
        }

        // 4. Générer le secret token
        String secretToken = UUID.randomUUID().toString().replace("-", "");
        String hashedSecret = SecurityUtils.hash(secretToken);

        // 5. Mettre à jour le device
        device.setSecretTokenHash(hashedSecret);
        device.setPairedAt(Instant.now());
        device.setStatus(DeviceStatus.OFFLINE);
        device.setPairingCode(null);
        device.setPairingCodeExpiresAt(null);

        deviceRepository.save(device);

        // 6. Retourner le secret token
        return new DevicePairResponse(
                device.getId().toString(),
                secretToken,
                device.getStatus().name()
        );
    }

    /**
     * SCÉNARIO: Vérifier le secret token lors de la connexion WebSocket
     *
     * @param deviceId ID du device
     * @param secretToken Secret token fourni par l'app
     * @return true si le token est valide
     */
    public boolean verifySecretToken(String deviceId, String secretToken) {
        try {
            UUID uuid = UUID.fromString(deviceId);
            Device device = deviceRepository.findById(uuid)
                    .orElse(null);

            if (device == null || device.getSecretTokenHash() == null) {
                return false;
            }

            return SecurityUtils.verify(secretToken, device.getSecretTokenHash());
        } catch (Exception e) {
            return false;
        }
    }
}