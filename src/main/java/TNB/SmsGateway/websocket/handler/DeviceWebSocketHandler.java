package TNB.SmsGateway.websocket.handler;

import TNB.SmsGateway.entity.Device;
import TNB.SmsGateway.entity.DeviceStatus;
import TNB.SmsGateway.entity.Message;
import TNB.SmsGateway.entity.MessageStatus;
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
import java.util.UUID;

/**
 * HANDLER: DeviceWebSocketHandler
 *
 * DESCRIPTION: Gère la communication WebSocket avec les devices Android
 * - Authentification du device via secretToken
 * - Réception des messages (heartbeat, status, SMS entrants)
 * - Envoi des commandes (dispatch SMS, requête SIM)
 * - Gestion des connexions/déconnexions
 *
 * SCÉNARIOS:
 * 1. Connexion: device se connecte avec deviceId + secretToken
 * 2. Heartbeat: device envoie un ping toutes les 30s
 * 3. Status: device confirme l'envoi d'un SMS (SENT/DELIVERED/FAILED)
 * 4. SMS entrant: device envoie un SMS reçu
 * 5. Dispatch: backend envoie une commande d'envoi de SMS
 * 6. Déconnexion: device se déconnecte → status OFFLINE
 */
@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DeviceWebSocketHandler.class);

    private final DeviceSessionManager sessionManager;
    private final DeviceService deviceService;
    private final DeviceStatusService deviceStatusService;
    private final IncomingMessageService incomingMessageService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public DeviceWebSocketHandler(DeviceSessionManager sessionManager,
                                  DeviceService deviceService,
                                  DeviceStatusService deviceStatusService,
                                  IncomingMessageService incomingMessageService,
                                  MessageService messageService) {
        this.sessionManager = sessionManager;
        this.deviceService = deviceService;
        this.deviceStatusService = deviceStatusService;
        this.incomingMessageService = incomingMessageService;
        this.messageService = messageService;
        this.objectMapper = new ObjectMapper();
    }

    // =============================================
    // ===== GESTION DES CONNEXIONS =====
    // =============================================

    /**
     * SCÉNARIO: Un device se connecte au WebSocket
     * ÉTAPES:
     * 1. Récupérer deviceId et secretToken depuis l'URL
     * 2. Vérifier le secret token (comparaison BCrypt)
     * 3. Enregistrer la session
     * 4. Mettre à jour le status à ONLINE
     * 5. Envoyer AUTH_SUCCESS
     *
     * @param session Session WebSocket
     */
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

        // Vérifier le secret token
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

        // Enregistrer la session
        sessionManager.registerSession(deviceId, session);

        // Mettre à jour le status
        deviceStatusService.updateStatus(deviceId, DeviceStatus.ONLINE);

        // Envoyer confirmation
        WebSocketMessage<AuthResponse> success = new WebSocketMessage<>(
                WebSocketMessageType.AUTH_SUCCESS,
                new AuthResponse(true, "Authentifié avec succès", deviceId.toString(), DeviceStatus.ONLINE.name())
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(success)));

        log.info("Device {} connecté", deviceId);
    }

    /**
     * SCÉNARIO: Un device se déconnecte
     * ÉTAPES:
     * 1. Récupérer le deviceId
     * 2. Marquer le device comme OFFLINE
     * 3. Supprimer la session
     *
     * @param session Session WebSocket
     * @param status Statut de fermeture
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());

        if (deviceId != null) {
            deviceStatusService.markOffline(deviceId);
            sessionManager.removeSession(deviceId);
            log.info("Device {} déconnecté", deviceId);
        }
    }

    /**
     * SCÉNARIO: Erreur de transport
     *
     * @param session Session WebSocket
     * @param exception Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Erreur de transport pour session {}", session.getId(), exception);
        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());

        if (deviceId != null) {
            deviceStatusService.markOffline(deviceId);
            sessionManager.removeSession(deviceId);
        }

        session.close(CloseStatus.SERVER_ERROR);
    }

    // =============================================
    // ===== TRAITEMENT DES MESSAGES =====
    // =============================================

    /**
     * SCÉNARIO: Le device envoie un message
     * ÉTAPES:
     * 1. Parser le message JSON
     * 2. Identifier le type (HEARTBEAT, DEVICE_SIMS_REPORT, etc.)
     * 3. Appeler le handler correspondant
     *
     * @param session Session WebSocket
     * @param message Message reçu
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Message reçu: {}", payload);

        // Parser le message
        WebSocketMessage<Map<String, Object>> wsMessage = objectMapper.readValue(
                payload,
                WebSocketMessage.class
        );

        String type = wsMessage.getType();
        Map<String, Object> data = wsMessage.getPayload();

        // Récupérer le deviceId
        UUID deviceId = sessionManager.getDeviceIdBySession(session.getId());
        if (deviceId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // Traiter selon le type
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

    /**
     * SCÉNARIO: Le device envoie un heartbeat (toutes les 30s)
     * ÉTAPES:
     * 1. Mettre à jour le timestamp du heartbeat
     * 2. Le device reste ONLINE
     *
     * @param deviceId ID du device
     */
    private void handleHeartbeat(UUID deviceId) {
        deviceStatusService.updateHeartbeat(deviceId);
        log.debug("Heartbeat reçu de device {}", deviceId);
    }

    /**
     * SCÉNARIO: Le device envoie la liste des SIMs détectées
     * ÉTAPES:
     * 1. Parser le rapport des SIMs
     * 2. Mettre à jour les SIMs du device
     * 3. Activer/désactiver les SIMs selon la présence
     *
     * @param deviceId ID du device
     * @param data Données du rapport
     */
    private void handleDeviceSimsReport(UUID deviceId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            DeviceSimReport report = objectMapper.readValue(json, DeviceSimReport.class);

            log.info("Rapport SIMs reçu de device {}: {} SIMs", deviceId, report.sims().size());

            // Traiter chaque SIM
            for (DeviceSimReport.SimInfo sim : report.sims()) {
                log.debug("SIM slot {}: opérateur {}, numéro {}, active: {}",
                        sim.slotIndex(), sim.operatorCode(), sim.phoneNumber(), sim.isActive());
                // Ici: mettre à jour les SIMs en base
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement du rapport SIMs", e);
        }
    }

    /**
     * SCÉNARIO: Le device confirme le status d'un SMS
     * ÉTAPES:
     * 1. Parser le status (SENT, DELIVERED, FAILED)
     * 2. Mettre à jour le message en base
     * 3. Enregistrer la date de délivrance si DELIVERED
     *
     * @param deviceId ID du device
     * @param data Données du status
     */
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

    /**
     * SCÉNARIO: Le device a reçu un SMS entrant
     * ÉTAPES:
     * 1. Parser le SMS (from, body, simSlot)
     * 2. Résoudre le client via device.owner
     * 3. Créer le message INBOUND
     * 4. Envoyer au webhook du client
     *
     * @param deviceId ID du device
     * @param data Données du SMS
     */
    private void handleIncomingSms(UUID deviceId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            IncomingSmsMessage incoming = objectMapper.readValue(json, IncomingSmsMessage.class);

            log.info("SMS entrant de {} sur device {} (slot {})",
                    incoming.from(), deviceId, incoming.simSlot());

            // Convertir en Map pour le service existant
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

    /**
     * SCÉNARIO: Envoyer un message générique à un device
     *
     * @param deviceId ID du device
     * @param type Type de message
     * @param data Données du message
     */
    public void sendToDevice(UUID deviceId, String type, Object data) {
        try {
            WebSocketMessage<Object> message = new WebSocketMessage<>(type, data);
            String json = objectMapper.writeValueAsString(message);
            sessionManager.sendToDevice(deviceId, json);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi au device {}", deviceId, e);
        }
    }

    /**
     * SCÉNARIO: Envoyer une commande d'envoi de SMS
     * Utilisé par le MessageService après le routage
     *
     * @param deviceId ID du device
     * @param messageId ID du message
     * @param to Numéro de destination
     * @param body Corps du message
     */
    public void dispatchSms(UUID deviceId, String messageId, String to, String body) {
        DispatchMessage dispatch = new DispatchMessage(messageId, to, body);
        sendToDevice(deviceId, WebSocketMessageType.DISPATCH_SMS.getValue(), dispatch);
        log.info("Commande DISPATCH_SMS envoyée au device {} pour le message {}", deviceId, messageId);
    }

    /**
     * SCÉNARIO: Envoyer une requête de rapport SIM
     *
     * @param deviceId ID du device
     */
    public void requestSimsReport(UUID deviceId) {
        sendToDevice(deviceId, WebSocketMessageType.REQUEST_SIMS_REPORT.getValue(), null);
        log.info("Requête REQUEST_SIMS_REPORT envoyée au device {}", deviceId);
    }

    /**
     * SCÉNARIO: Envoyer une erreur à un device
     *
     * @param session Session WebSocket
     * @param message Message d'erreur
     */
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

    /**
     * Vérifier le secret token d'un device
     * Utilise BCrypt pour la comparaison sécurisée
     *
     * @param deviceId ID du device
     * @param secretToken Secret token fourni par l'app
     * @return true si valide
     */
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