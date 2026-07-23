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
    public void handleIncomingMessage(UUID deviceId, Message message) {
        // 1. Récupérer le device
        Device device = deviceService.findById(deviceId);
        User user = device.getUser();

        webhookService.sendToWebhook(message, user);
    }
}