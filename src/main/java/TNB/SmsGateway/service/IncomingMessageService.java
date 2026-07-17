package TNB.SmsGateway.service;


import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.MessageDirection;
import TNB.SmsGateway.entity.MessageStatus;
import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class IncomingMessageService {

    private final MessageRepository messageRepository;
    private final WebhookService webhookService;
    private final DeviceService deviceService;
    private final UserService userService;

    public IncomingMessageService(MessageRepository messageRepository,
                                  WebhookService webhookService,
                                  DeviceService deviceService,
                                  UserService userService) {
        this.messageRepository = messageRepository;
        this.webhookService = webhookService;
        this.deviceService = deviceService;
        this.userService = userService;
    }

    /**
     * Traiter un SMS entrant reçu du device
     * Scénario: Device reçoit un SMS → on le traite
     */
    @Transactional
    public void handleIncomingMessage(UUID deviceId, Map<String, Object> data) {
        // 1. Récupérer le device
        Device device = deviceService.findById(deviceId);
        User user = device.getUser();

        // 2. Récupérer la SIM
        Integer slotIndex = (Integer) data.get("simSlot");
        DeviceSim sim = device.getSims().stream()
                .filter(s -> s.getSlotIndex().equals(slotIndex))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SIM non trouvée"));

        // 3. Créer le message entrant
        Message message = new Message();
        message.setUser(user);
        message.setDirection(MessageDirection.INBOUND);
        message.setFromNumber((String) data.get("from"));
        message.setToNumber(sim.getPhoneNumber());
        message.setBody((String) data.get("body"));
        message.setCountryCode(device.getCountry().getCode());
        message.setOperatorCode(sim.getOperator().getCode());
        message.setStatus(MessageStatus.DELIVERED);
        message.setDevice(device);
        message.setDeviceSim(sim);

        message = messageRepository.save(message);

        // 4. Envoyer au webhook
        webhookService.sendToWebhook(message, user);
    }
}