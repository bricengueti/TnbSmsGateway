package TNB.SmsGateway.websocket.handler;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceSim;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.MessageStatus;
import TNB.SmsGateway.entity.Operator;
import TNB.SmsGateway.repository.DeviceSimRepository;
import TNB.SmsGateway.service.*;
import TNB.SmsGateway.websocket.WebSocketMessage;
import TNB.SmsGateway.websocket.WebSocketMessageType;
import TNB.SmsGateway.websocket.dto.*;
import TNB.SmsGateway.websocket.session.DeviceSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DeviceWebSocketHandler.class);

    private final DeviceSessionManager sessionManager;
    private final DeviceService deviceService;
    private final DeviceStatusService deviceStatusService;
    private final IncomingMessageService incomingMessageService;
    private final MessageService messageService;
    private final DeviceSimRepository deviceSimRepository;
    private final ReferenceService referenceService;
    private final ObjectMapper objectMapper;

    public DeviceWebSocketHandler(DeviceSessionManager sessionManager,
                                  DeviceService deviceService,
                                  DeviceStatusService deviceStatusService,
                                  IncomingMessageService incomingMessageService,
                                  MessageService messageService,
                                  DeviceSimRepository deviceSimRepository,
                                  ReferenceService referenceService) {
        this.sessionManager = sessionManager;
        this.deviceService = deviceService;
        this.deviceStatusService = deviceStatusService;
        this.incomingMessageService = incomingMessageService;
        this.messageService = messageService;
        this.deviceSimRepository = deviceSimRepository;
        this.referenceService = referenceService;
        this.objectMapper = new ObjectMapper();
    }

    // =============================================
    // ===== GESTION DES CONNEXIONS =====
    // =============================================

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        String deviceIdStr = (String) attributes.get("deviceId");
        String secretToken = (String) attributes.get("secretToken");

        if (deviceIdStr == null || secretToken == null) {
            log.warn("Connexion sans deviceId ou secretToken");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        UUID deviceId = UUID.fromString(deviceIdStr);

        boolean isValid = verifySecretToken(deviceId, secretToken);
        if (!isValid) {
            log.warn("Authentification échouée pour device {}", deviceId);
            WebSocketMessage<String> error = new WebSocketMessage<>(
                    WebSocketMessageType.AUTH_FAILURE,
                    "Token invalide"
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 🔥 SÉCURITÉ SESSION UNIQUE : Si une ancienne session existe pour ce device, on la ferme explicitement d'abord
        WebSocketSession oldSession = sessionManager.getSession(deviceId);
        if (oldSession != null && !oldSession.getId().equals(session.getId())) {
            log.info("Session fantôme détectée pour le device {}. Fermeture immédiate de l'ancienne liaison.", deviceId);
            try {
                oldSession.close(CloseStatus.POLICY_VIOLATION);
            } catch (Exception e) {
                log.debug("Erreur lors de la fermeture de la session fantôme", e);
            }
        }

        sessionManager.registerSession(deviceId, session);
        deviceStatusService.updateStatus(deviceId, DeviceStatus.ONLINE);

        WebSocketMessage<AuthResponse> success = new WebSocketMessage<>(
                WebSocketMessageType.AUTH_SUCCESS,
                new AuthResponse(true, "Authentifié avec succès", deviceId.toString(), DeviceStatus.ONLINE.name())
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(success)));

        log.info("Device {} connecté avec succès (Session unique)", deviceId);

        // 🔄 AUTOMATION : Dès que l'authentification réussit, on demande immédiatement un rapport SIM actualisé au device Android
        requestSimsReport(deviceId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());

        if (deviceId != null) {
            // 🔥 CRITIQUE : On ne passe OFFLINE que si la session qui se ferme est bien la session ACTUELLE stockée dans le manager.
            // Si c'est une vieille session fantôme qui se ferme alors qu'une nouvelle est active, on ne touche à rien !
            WebSocketSession currentActiveSession = sessionManager.getSession(deviceId);
            if (currentActiveSession != null && currentActiveSession.getId().equals(session.getId())) {
                deviceStatusService.markOffline(deviceId);
                sessionManager.removeSession(deviceId);
                log.info("Device {} déconnecté proprement", deviceId);
            } else {
                // C'était une session fantôme mourante, on nettoie juste son index inversé sans impacter le statut ONLINE global
                sessionManager.removeSessionBySessionId(session.getId());
                log.info("Ancienne session déloguée pour le device {} (la nouvelle session reste active)", deviceId);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Erreur de transport pour session {}", session.getId(), exception);
        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());

        if (deviceId != null) {
            WebSocketSession currentActiveSession = sessionManager.getSession(deviceId);
            if (currentActiveSession != null && currentActiveSession.getId().equals(session.getId())) {
                deviceStatusService.markOffline(deviceId);
                sessionManager.removeSession(deviceId);
            } else {
                sessionManager.removeSessionBySessionId(session.getId());
            }
        }
        session.close(CloseStatus.SERVER_ERROR);
    }

    // =============================================
    // ===== TRAITEMENT DES MESSAGES =====
    // =============================================

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Message reçu: {}", payload);

        WebSocketMessage<Map<String, Object>> wsMessage = objectMapper.readValue(
                payload,
                WebSocketMessage.class
        );

        String type = wsMessage.getType();
        Map<String, Object> data = wsMessage.getPayload();

        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());
        if (deviceId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        WebSocketMessageType messageType = WebSocketMessageType.fromValue(type);
        if (messageType == null) {
            log.warn("Type de message inconnu: {}", type);
            sendError(session, "Type de message inconnu: " + type);
            return;
        }

        switch (messageType) {
            case HEARTBEAT -> handleHeartbeat(deviceId);
            case DEVICE_SIMS_REPORT -> handleDeviceSimsReport(deviceId, data);
            case SMS_STATUS_UPDATE -> handleSmsStatusUpdate(deviceId, data);
            case INCOMING_SMS -> handleIncomingSms(deviceId, data);
            default -> {
                log.warn("Type de message non géré: {}", type);
                sendError(session, "Type de message non géré: " + type);
            }
        }
    }

    // =============================================
    // ===== HANDLERS SPÉCIFIQUES =====
    // =============================================

    private void handleHeartbeat(UUID deviceId) {
        deviceStatusService.updateHeartbeat(deviceId);
        log.debug("Heartbeat reçu de device {}", deviceId);
    }

    private void handleDeviceSimsReport(UUID deviceId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            DeviceSimReport report = objectMapper.readValue(json, DeviceSimReport.class);

            log.info("Rapport SIMs reçu de device {}: {} SIMs", deviceId, report.sims().size());

            Device device = deviceService.findById(deviceId);
            String countryCode = device.getCountry().getCode();

            for (DeviceSimReport.SimInfo simInfo : report.sims()) {
                log.debug("Traitement SIM slot {}: opérateur brut '{}', numéro {}, active: {}, quota: {}",
                        simInfo.slotIndex(), simInfo.operatorCode(), simInfo.phoneNumber(),
                        simInfo.isActive(), simInfo.dailyQuota());

                DeviceSim sim = deviceSimRepository
                        .findByDeviceAndSlotIndex(device, simInfo.slotIndex())
                        .orElseGet(() -> {
                            DeviceSim newSim = new DeviceSim();
                            newSim.setDevice(device);
                            newSim.setSlotIndex(simInfo.slotIndex());
                            log.info("Nouvelle SIM créée pour device {} au slot {}",
                                    deviceId, simInfo.slotIndex());
                            return newSim;
                        });

                if (simInfo.operatorCode() != null && !simInfo.operatorCode().isBlank()) {
                    Optional<Operator> operator = referenceService.resolveOperatorFromRawName(
                            simInfo.operatorCode(), countryCode);
                    if (operator.isPresent()) {
                        sim.setOperator(operator.get());
                    } else {
                        log.warn("Aucun opérateur du pays {} ne correspond à '{}' pour device {} slot {}",
                                countryCode, simInfo.operatorCode(), deviceId, simInfo.slotIndex());
                    }
                }

                if (sim.getOperator() == null) {
                    log.error("Impossible de sauvegarder la SIM slot {} pour device {}: aucun opérateur résolu",
                            simInfo.slotIndex(), deviceId);
                    continue;
                }

                sim.setPhoneNumber(simInfo.phoneNumber());
                sim.setIsActive(simInfo.isActive() != null ? simInfo.isActive() : true);

                if (simInfo.dailyQuota() != null && !simInfo.dailyQuota().isBlank()) {
                    sim.setDailySmsQuota(simInfo.dailyQuota());
                }

                deviceSimRepository.save(sim);
                log.debug("SIM slot {} sauvegardée pour device {}", simInfo.slotIndex(), deviceId);
            }

            log.info("Rapport SIMs traité avec succès pour device {}: {} SIMs persistées",
                    deviceId, report.sims().size());

        } catch (Exception e) {
            log.error("Erreur lors du traitement du rapport SIMs pour device {}", deviceId, e);
        }
    }

    private void handleSmsStatusUpdate(UUID deviceId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            StatusUpdateMessage statusUpdate = objectMapper.readValue(json, StatusUpdateMessage.class);

            log.info("Status update pour message {}: {} sur device {}",
                    statusUpdate.messageId(), statusUpdate.status(), deviceId);

            UUID messageId = UUID.fromString(statusUpdate.messageId());
            Message message = messageService.findById(messageId);

            switch (statusUpdate.status()) {
                case "SENT" -> {
                    message.setStatus(MessageStatus.SENT);
                    log.info("Message {} marqué comme SENT", messageId);
                }
                case "DELIVERED" -> {
                    message.setStatus(MessageStatus.DELIVERED);
                    message.setDeliveredAt(Instant.now());
                    log.info("Message {} marqué comme DELIVERED à {}", messageId, message.getDeliveredAt());
                }
                case "FAILED" -> {
                    message.setStatus(MessageStatus.FAILED);
                    message.setErrorReason(statusUpdate.errorReason());
                    log.info("Message {} marqué comme FAILED: {}", messageId, statusUpdate.errorReason());
                }
                default -> log.warn("Status inconnu: {}", statusUpdate.status());
            }

            messageService.save(message);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du status", e);
        }
    }

    private void handleIncomingSms(UUID deviceId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            IncomingSmsMessage incoming = objectMapper.readValue(json, IncomingSmsMessage.class);

            log.info("SMS entrant de {} sur device {} (slot {})",
                    incoming.from(), deviceId, incoming.simSlot());

            Map<String, Object> dataMap = Map.of(
                    "from", incoming.from(),
                    "body", incoming.body(),
                    "simSlot", incoming.simSlot(),
                    "receivedAt", incoming.receivedAt() != null ? incoming.receivedAt() : Instant.now().toString()
            );

            incomingMessageService.handleIncomingMessage(deviceId, dataMap);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du SMS entrant", e);
        }
    }

    // =============================================
    // ===== ENVOI DE MESSAGES AUX DEVICES =====
    // =============================================

    public void sendToDevice(UUID deviceId, String type, Object data) {
        try {
            WebSocketMessage<Object> message = new WebSocketMessage<>(type, data);
            String json = objectMapper.writeValueAsString(message);
            sessionManager.sendToDevice(deviceId, json);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi au device {}", deviceId, e);
        }
    }

    public void dispatchSms(UUID deviceId, String slotIndex, String messageId, String to, String body) {
        DispatchMessage dispatch = new DispatchMessage(messageId, slotIndex ,to, body);
        sendToDevice(deviceId, WebSocketMessageType.DISPATCH_SMS.getValue(), dispatch);
        log.info("Commande DISPATCH_SMS envoyée au device {} pour le message {} au slot {} ", deviceId, messageId, slotIndex);
    }

    public void requestSimsReport(UUID deviceId) {
        sendToDevice(deviceId, WebSocketMessageType.REQUEST_SIMS_REPORT.getValue(), null);
        log.info("Requête REQUEST_SIMS_REPORT envoyée au device {}", deviceId);
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            WebSocketMessage<String> error = new WebSocketMessage<>(
                    WebSocketMessageType.ERROR,
                    message
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'erreur", e);
        }
    }

    // =============================================
    // ===== HELPERS =====
    // =============================================

    private boolean verifySecretToken(UUID deviceId, String secretToken) {
        try {
            Device device = deviceService.findById(deviceId);
            return device.getSecretTokenHash() != null &&
                    org.springframework.security.crypto.bcrypt.BCrypt.checkpw(
                            secretToken,
                            device.getSecretTokenHash()
                    );
        } catch (Exception e) {
            return false;
        }
    }
}