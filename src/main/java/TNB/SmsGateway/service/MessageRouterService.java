package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.MessageStatus;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.exception.message.NoDeviceForCountryOperatorException;
import TNB.SmsGateway.repository.DeviceRepository;
import TNB.SmsGateway.repository.DeviceSimRepository;
import TNB.SmsGateway.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SERVICE: MessageRouterService
 *
 * DESCRIPTION: Gère le routage des messages vers les devices
 * - Recherche de devices disponibles par pays/opérateur
 * - Sélection de la SIM la moins utilisée
 * - Dispatch du message vers le device
 * - Réassignation en cas d'échec
 *
 * SCÉNARIOS:
 * 1. Routage: trouver un device pour le couple pays/opérateur
 * 2. Sélection: choisir la SIM avec le plus de quota
 * 3. Dispatch: envoyer le message au device
 * 4. Réassignation: en cas d'échec, réessayer un autre device
 */
@Service
public class MessageRouterService {

    private final DeviceRepository deviceRepository;
    private final DeviceSimRepository deviceSimRepository;
    private final MessageRepository messageRepository;

    public MessageRouterService(DeviceRepository deviceRepository,
                                DeviceSimRepository deviceSimRepository,
                                MessageRepository messageRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceSimRepository = deviceSimRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * SCÉNARIO: Trouver un device disponible
     * ÉTAPES:
     * 1. Rechercher tous les devices pour le pays/opérateur
     * 2. Filtrer: ONLINE + SIM active + quota disponible
     * 3. Trier par usage croissant (round-robin)
     * 4. Retourner le device le moins utilisé
     *
     * @param userId ID de l'utilisateur
     * @param countryCode Code pays
     * @param operatorCode Code opérateur
     * @return Device disponible
     * @throws NoDeviceForCountryOperatorException Si aucun device
     */
    public Device findAvailableDevice(UUID userId, String countryCode, String operatorCode) {
        List<Device> devices = deviceRepository.findAvailableDevices(userId, countryCode, operatorCode);

        if (devices.isEmpty()) {
            throw new NoDeviceForCountryOperatorException(countryCode, operatorCode);
        }

        // Retourner le device le moins utilisé
        return devices.get(0);
    }

    /**
     * SCÉNARIO: Trouver une SIM disponible sur un device
     *
     * @param device Device contenant les SIMs
     * @return SIM active avec quota disponible
     * @throws BusinessException Si aucune SIM disponible
     */
    public DeviceSim findAvailableSim(Device device) {
        return device.getSims().stream()
                .filter(DeviceSim::getIsActive)
                .filter(DeviceSim::hasQuota)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Aucune SIM disponible sur ce device", "NO_SIM_AVAILABLE", 422));
    }

    /**
     * SCÉNARIO: Dispatcher un message vers un device
     * ÉTAPES:
     * 1. Marquer le message comme DISPATCHED
     * 2. Associer le device et la SIM
     * 3. Sauvegarder
     * 4. Incrémenter le compteur de la SIM
     *
     * @param message Message à dispatcher
     * @param device Device cible
     * @param sim SIM utilisée
     * @return Message dispatche
     */
    @Transactional
    public Message dispatchMessage(Message message, Device device, DeviceSim sim) {
        // 1. Marquer le message comme dispatché
        message.markDispatched(device, sim);
        message.setDispatchedAt(Instant.now());

        // 2. Sauvegarder
        Message saved = messageRepository.save(message);

        // 3. Incrémenter le compteur de la SIM
        sim.incrementDailySmsSent();
        deviceSimRepository.save(sim);

        return saved;
    }

    /**
     * SCÉNARIO: Réassigner un message en échec
     * ÉTAPES:
     * 1. Incrémenter les tentatives
     * 2. Si max tentatives atteint → FAILED
     * 3. Sinon, trouver un autre device disponible
     * 4. Réassigner le message
     *
     * @param message Message à réassigner
     * @return Optional contenant le message réassigné ou vide
     */
    @Transactional
    public Optional<Message> reassignMessage(Message message) {
        // 1. Incrémenter les tentatives
        message.incrementAttempts();

        // 2. Vérifier si on peut encore réessayer
        if (message.getAttempts() >= 3) {
            message.markFailed("Max retries exceeded");
            messageRepository.save(message);
            return Optional.empty();
        }

        // 3. Trouver un autre device disponible
        try {
            Device newDevice = findAvailableDevice(
                    message.getUser().getId(),
                    message.getCountryCode(),
                    message.getOperatorCode()
            );

            DeviceSim newSim = findAvailableSim(newDevice);

            // 4. Réassigner
            message.markDispatched(newDevice, newSim);
            message.setDispatchedAt(Instant.now());

            // 5. Incrémenter le compteur de la nouvelle SIM
            newSim.incrementDailySmsSent();
            deviceSimRepository.save(newSim);

            return Optional.of(messageRepository.save(message));

        } catch (NoDeviceForCountryOperatorException e) {
            // Plus de device disponible
            message.markFailed("No device available for reassignment");
            messageRepository.save(message);
            return Optional.empty();
        }
    }

    /**
     * SCÉNARIO: Vérifier si un message peut être réassigné
     *
     * @param message Message à vérifier
     * @return true si réassignable
     */
    public boolean canReassign(Message message) {
        return message.getAttempts() < 3 &&
                message.getStatus() != MessageStatus.DELIVERED &&
                message.getStatus() != MessageStatus.FAILED;
    }

    /**
     * SCÉNARIO: Trouver les messages à réassigner sur un device
     *
     * @param deviceId ID du device
     * @return Liste des messages à réassigner
     */
    public List<Message> findMessagesToReassign(UUID deviceId) {
        return messageRepository.findDispatchMessagesByDevice(deviceId);
    }
}