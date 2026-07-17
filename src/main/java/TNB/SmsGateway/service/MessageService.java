package TNB.SmsGateway.service;


import TNB.SmsGateway.dto.request.SendMessageRequest;
import TNB.SmsGateway.dto.response.MessageResponse;
import TNB.SmsGateway.entity.*;
import TNB.SmsGateway.exception.BusinessException;
import TNB.SmsGateway.exception.message.OperatorCountryMismatchException;
import TNB.SmsGateway.repository.MessageRepository;
import TNB.SmsGateway.websocket.handler.DeviceWebSocketHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SERVICE: MessageService
 *
 * DESCRIPTION: Gère l'envoi et la réception des SMS
 * - Envoi de SMS (validation, routage, dispatch)
 * - Consultation des messages
 * - Mise à jour des status
 * - Idempotence (idempotencyKey)
 * - Réception des SMS entrants (via WebSocket)
 *
 * SCÉNARIOS:
 * 1. Envoi SMS: client → validation → routage → dispatch → confirmation
 * 2. Status: SENT → DELIVERED ou FAILED
 * 3. Idempotence: même clé → retour du même messageId
 * 4. Consultation: messages paginés par utilisateur
 * 5. Réception: SMS entrant → webhook
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageRouterService messageRouter;
    private final ReferenceService referenceService;
    private final UserService userService;
    private final DeviceWebSocketHandler webSocketHandler;

    public MessageService(MessageRepository messageRepository,
                          MessageRouterService messageRouter,
                          ReferenceService referenceService,
                          UserService userService,
                          DeviceWebSocketHandler webSocketHandler) {
        this.messageRepository = messageRepository;
        this.messageRouter = messageRouter;
        this.referenceService = referenceService;
        this.userService = userService;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * SCÉNARIO: Envoyer un SMS via l'API
     * ÉTAPES:
     * 1. Valider le numéro E.164
     * 2. Vérifier que le pays existe
     * 3. Vérifier que l'opérateur appartient au pays
     * 4. Vérifier l'idempotence (idempotencyKey)
     * 5. Créer le message (status PENDING)
     * 6. Router vers un device disponible
     * 7. Dispatcher vers le device via WebSocket
     * 8. Retourner la réponse
     *
     * @param userId ID de l'utilisateur
     * @param request Détails du message
     * @return MessageResponse avec messageId et status
     * @throws BusinessException Si validation échoue
     * @throws OperatorCountryMismatchException Si opérateur/pays incompatible
     */
    @Transactional
    public MessageResponse sendMessage(UUID userId, SendMessageRequest request) {
        User user = userService.findByIdOrThrow(userId);

        // 1. Valider le numéro E.164 (validation simple)
        String toNumber = request.to();
        if (toNumber == null || !toNumber.startsWith("+") || toNumber.length() < 6 || toNumber.length() > 16) {
            throw new BusinessException("Numéro de téléphone invalide. Doit être au format E.164 (ex: +237699999999)",
                    "INVALID_PHONE_NUMBER", 400);
        }

        // 2. Vérifier que le pays existe
        referenceService.findCountryByCode(request.countryCode())
                .orElseThrow(() -> new BusinessException("Pays non trouvé", "COUNTRY_NOT_FOUND", 404));

        // 3. Vérifier que l'opérateur appartient au pays
        boolean operatorValid = referenceService.isOperatorInCountry(request.operator(), request.countryCode());
        if (!operatorValid) {
            throw new OperatorCountryMismatchException(request.operator(), request.countryCode());
        }

        // 4. Vérifier l'idempotence
        if (request.idempotencyKey() != null && !request.idempotencyKey().isEmpty()) {
            Message existing = messageRepository
                    .findByUserAndIdempotencyKey(user, request.idempotencyKey())
                    .orElse(null);
            if (existing != null) {
                return toMessageResponse(existing);
            }
        }

        // 5. Créer le message
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

        // 6. Router vers un device
        Device device = messageRouter.findAvailableDevice(userId, request.countryCode(), request.operator());
        DeviceSim sim = messageRouter.findAvailableSim(device);

        // 7. Dispatcher
        message = messageRouter.dispatchMessage(message, device, sim);

        // 8. Envoyer la commande au device via WebSocket
        webSocketHandler.dispatchSms(
                device.getId(),
                message.getId().toString(),
                message.getToNumber(),
                message.getBody()
        );

        return toMessageResponse(message);
    }

    // ===== MÉTHODES DE CONSULTATION =====

    /**
     * SCÉNARIO: Trouver un message par son ID
     *
     * @param messageId ID du message
     * @return Message trouvé
     */
    public Message findById(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message non trouvé", "MESSAGE_NOT_FOUND", 404));
    }

    /**
     * SCÉNARIO: Trouver un message avec vérification d'appartenance
     *
     * @param messageId ID du message
     * @param userId ID de l'utilisateur
     * @return Message trouvé
     */
    public Message findByIdAndUser(UUID messageId, UUID userId) {
        return messageRepository.findById(messageId)
                .filter(m -> m.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("Message non trouvé", "MESSAGE_NOT_FOUND", 404));
    }

    /**
     * SCÉNARIO: Sauvegarder un message (mise à jour status)
     *
     * @param message Message à sauvegarder
     * @return Message sauvegardé
     */
    @Transactional
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    /**
     * SCÉNARIO: Mettre à jour le status d'un message
     *
     * @param messageId ID du message
     * @param status Nouveau status
     */
    @Transactional
    public void updateStatus(UUID messageId, MessageStatus status) {
        messageRepository.updateStatus(messageId, status);
    }

    /**
     * SCÉNARIO: Marquer un message comme délivré
     *
     * @param messageId ID du message
     */
    @Transactional
    public void markAsDelivered(UUID messageId) {
        Message message = findById(messageId);
        message.markDelivered();
        messageRepository.save(message);
    }

    /**
     * SCÉNARIO: Marquer un message comme échoué
     *
     * @param messageId ID du message
     * @param reason Raison de l'échec
     */
    @Transactional
    public void markAsFailed(UUID messageId, String reason) {
        Message message = findById(messageId);
        message.markFailed(reason);
        messageRepository.save(message);
    }

    /**
     * SCÉNARIO: Récupérer les messages d'un utilisateur (paginated)
     *
     * @param userId ID de l'utilisateur
     * @param page Numéro de page
     * @param size Taille de la page
     * @return Page de messages
     */
    public Page<Message> getMessagesByUser(UUID userId, int page, int size) {
        User user = userService.findByIdOrThrow(userId);
        return messageRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
    }

    /**
     * SCÉNARIO: Récupérer les messages en attente (pour retry)
     *
     * @return Liste des messages en attente
     */
    public List<Message> getPendingMessages() {
        return messageRepository.findPendingMessages();
    }

    /**
     * SCÉNARIO: Incrémenter les tentatives d'un message
     *
     * @param messageId ID du message
     */
    @Transactional
    public void incrementAttempts(UUID messageId) {
        messageRepository.incrementAttempts(messageId);
    }

    // ===== UTILITAIRES =====

    /**
     * Convertir un Message en MessageResponse
     */
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