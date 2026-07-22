package TNB.SmsGateway.service;

import TNB.SmsGateway.dto.request.SendMessageRequest;
import TNB.SmsGateway.dto.response.MessageResponse;
import TNB.SmsGateway.entity.*;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.exception.message.OperatorCountryMismatchException;
import TNB.SmsGateway.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageRouterService messageRouter;
    private final ReferenceService referenceService;
    private final UserService userService;
    private final SmsDispatchScheduler smsDispatchScheduler;

    public MessageService(MessageRepository messageRepository,
                          MessageRouterService messageRouter,
                          ReferenceService referenceService,
                          UserService userService,
                          SmsDispatchScheduler smsDispatchScheduler) {
        this.messageRepository = messageRepository;
        this.messageRouter = messageRouter;
        this.referenceService = referenceService;
        this.userService = userService;
        this.smsDispatchScheduler = smsDispatchScheduler;
    }

    @Transactional
    public MessageResponse sendMessage(UUID userId, SendMessageRequest request) {
        User user = userService.findByIdOrThrow(userId);

        String toNumber = request.to();
        if (toNumber == null || !toNumber.startsWith("+") || toNumber.length() < 6 || toNumber.length() > 16) {
            throw new BusinessException("Numéro de téléphone invalide. Doit être au format E.164 (ex: +237699999999)",
                    "INVALID_PHONE_NUMBER", 400);
        }

        referenceService.findCountryByCode(request.countryCode())
                .orElseThrow(() -> new BusinessException("Pays non trouvé", "COUNTRY_NOT_FOUND", 404));

        boolean operatorValid = referenceService.isOperatorInCountry(request.operator(), request.countryCode());
        if (!operatorValid) {
            throw new OperatorCountryMismatchException(request.operator(), request.countryCode());
        }

        if (request.idempotencyKey() != null && !request.idempotencyKey().isEmpty()) {
            Message existing = messageRepository
                    .findByUserAndIdempotencyKey(user, request.idempotencyKey())
                    .orElse(null);
            if (existing != null) {
                return toMessageResponse(existing);
            }
        }

        Message message = new Message();
        message.setUser(user);
        message.setDirection(MessageDirection.OUTBOUND);
        message.setToNumber(request.to());
        message.setBody(request.body());
        message.setCountryCode(request.countryCode());
        message.setOperatorCode(request.operator());
        message.setPriority(request.priority() != null ? MessagePriority.valueOf(request.priority()) : MessagePriority.NORMAL);
        message.setStatus(MessageStatus.PENDING);
        message.setAttempts(0);
        message.setIdempotencyKey(request.idempotencyKey());

        message = messageRepository.save(message);

        Device device = messageRouter.findAvailableDevice(userId, request.countryCode(), request.operator());
        DeviceSim sim = messageRouter.findAvailableSim(device);

        message = messageRouter.dispatchMessage(message, device, sim);

        // ✅ MODIFIÉ : dispatch réel via le scheduler de cadence (délai
        // min/max effectif de la SIM, surcharge sinon fallback device),
        // au lieu d'un envoi WebSocket immédiat.
        int minDelaySec = sim.resolveEffectiveMinDelaySec();
        int maxDelaySec = sim.resolveEffectiveMaxDelaySec();

        smsDispatchScheduler.scheduleDispatch(
                device.getId(),
                sim.getId(),
                minDelaySec,
                maxDelaySec,
                message.getDeviceSim().getSlotIndex().toString(),
                message.getId().toString(),
                message.getToNumber(),
                message.getBody()
        );

        return toMessageResponse(message);
    }

    public Message findById(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message non trouvé", "MESSAGE_NOT_FOUND", 404));
    }

    public Message findByIdAndUser(UUID messageId, UUID userId) {
        return messageRepository.findById(messageId)
                .filter(m -> m.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("Message non trouvé", "MESSAGE_NOT_FOUND", 404));
    }

    @Transactional
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public void updateStatus(UUID messageId, MessageStatus status) {
        messageRepository.updateStatus(messageId, status);
    }

    @Transactional
    public void markAsDelivered(UUID messageId) {
        Message message = findById(messageId);
        message.markDelivered();
        messageRepository.save(message);
    }

    @Transactional
    public void markAsFailed(UUID messageId, String reason) {
        Message message = findById(messageId);
        message.markFailed(reason);
        messageRepository.save(message);
    }

    public Page<Message> getMessagesByUser(UUID userId, int page, int size) {
        User user = userService.findByIdOrThrow(userId);
        return messageRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
    }

    public List<Message> getPendingMessages() {
        return messageRepository.findPendingMessages();
    }

    @Transactional
    public void incrementAttempts(UUID messageId) {
        messageRepository.incrementAttempts(messageId);
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId().toString(),
                message.getDirection().name(),
                message.getToNumber(),
                message.getFromNumber(),
                message.getBody(),
                message.getCountryCode(),
                message.getOperatorCode(),
                message.getStatus().name(),
                message.getAttempts(),
                message.getErrorReason(),
                message.getDevice() != null ? message.getDevice().getId().toString() : null,
                message.getCreatedAt(),
                message.getDispatchedAt(),
                message.getDeliveredAt()
        );
    }
}