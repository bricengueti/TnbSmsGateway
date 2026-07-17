package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceStatusService {

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final MessageRouterService messageRouter;

    public DeviceStatusService(DeviceRepository deviceRepository,
                               DeviceService deviceService,
                               MessageRouterService messageRouter) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.messageRouter = messageRouter;
    }

    /**
     * Mettre à jour le status d'un device
     * Scénario: Connexion/déconnexion du device en WebSocket
     */
    @Transactional
    public void updateStatus(UUID deviceId, DeviceStatus status) {
        deviceRepository.updateStatus(deviceId, status);
    }

    /**
     * Mettre à jour le heartbeat
     * Scénario: Le device envoie un heartbeat toutes les 30s
     */
    @Transactional
    public void updateHeartbeat(UUID deviceId) {
        deviceRepository.updateHeartbeat(deviceId, Instant.now());

        // Si le device est OFFLINE, le passer à ONLINE
        Device device = deviceService.findById(deviceId);
        if (device.getStatus() == DeviceStatus.OFFLINE) {
            deviceRepository.updateStatus(deviceId, DeviceStatus.ONLINE);
        }
    }

    /**
     * Marquer un device comme offline (quand il se déconnecte)
     */
    @Transactional
    public void markOffline(UUID deviceId) {
        deviceRepository.markAsOffline(deviceId);
    }

    /**
     * Détecter et gérer les devices offline
     * Scénario: Tâche planifiée pour détecter les devices morts
     */
    @Transactional
    public void handleStaleDevices() {
        // Seuils: 2 minutes sans heartbeat
        Instant threshold = Instant.now().minusSeconds(120);
        List<Device> staleDevices = deviceRepository.findStaleDevices(threshold);

        for (Device device : staleDevices) {
            // 1. Marquer comme offline
            deviceRepository.markAsOffline(device.getId());

            // 2. Réassigner les messages en cours
            List<Message> messages = messageRouter.findMessagesToReassign(device.getId());
            for (Message message : messages) {
                messageRouter.reassignMessage(message);
            }
        }
    }
}