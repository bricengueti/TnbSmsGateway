package TNB.SmsGateway.websocket.session;

import TNB.SmsGateway.entity.Device;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MANAGER: DeviceSessionManager
 *
 * DESCRIPTION: Gère les sessions WebSocket des devices
 * - Map deviceId → WebSocketSession
 * - Map sessionId → deviceId
 * - Envoi de messages aux devices connectés
 * - Gestion des déconnexions
 *
 * SCÉNARIOS:
 * 1. Connexion: enregistrement de la session
 * 2. Envoi: envoi de messages à un device spécifique
 * 3. Broadcast: envoi à tous les devices
 * 4. Déconnexion: suppression de la session
 */
@Component
public class DeviceSessionManager {

    // Map: deviceId -> WebSocketSession
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Map: sessionId -> deviceId
    private final Map<String, UUID> sessionToDevice = new ConcurrentHashMap<>();

    /**
     * SCÉNARIO: Enregistrer une session lors de la connexion
     *
     * @param deviceId ID du device
     * @param session Session WebSocket
     */
    public void registerSession(UUID deviceId, WebSocketSession session) {
        sessions.put(deviceId, session);
        sessionToDevice.put(session.getId(), deviceId);
    }

    /**
     * SCÉNARIO: Supprimer une session lors de la déconnexion
     *
     * @param deviceId ID du device
     */
    public void removeSession(UUID deviceId) {
        WebSocketSession session = sessions.remove(deviceId);
        if (session != null) {
            sessionToDevice.remove(session.getId());
        }
    }

    /**
     * SCÉNARIO: Supprimer une session par son ID
     *
     * @param sessionId ID de la session
     */
    public void removeSessionBySessionId(String sessionId) {
        UUID deviceId = sessionToDevice.remove(sessionId);
        if (deviceId != null) {
            sessions.remove(deviceId);
        }
    }

    /**
     * SCÉNARIO: Récupérer une session
     *
     * @param deviceId ID du device
     * @return WebSocketSession ou null
     */
    public WebSocketSession getSession(UUID deviceId) {
        return sessions.get(deviceId);
    }

    /**
     * SCÉNARIO: Récupérer le deviceId à partir d'une session
     *
     * @param sessionId ID de la session
     * @return deviceId ou null
     */
    public UUID getDeviceIdBySession(String sessionId) {
        return sessionToDevice.get(sessionId);
    }

    /**
     * SCÉNARIO: Vérifier si un device est connecté
     *
     * @param deviceId ID du device
     * @return true si connecté
     */
    public boolean isDeviceConnected(UUID deviceId) {
        return sessions.containsKey(deviceId);
    }

    /**
     * SCÉNARIO: Compter les sessions actives
     *
     * @return nombre de sessions
     */
    public int getActiveSessionsCount() {
        return sessions.size();
    }

    /**
     * SCÉNARIO: Envoyer un message à un device spécifique
     *
     * @param deviceId ID du device
     * @param message Message JSON
     */
    public void sendToDevice(UUID deviceId, String message) {
        WebSocketSession session = getSession(deviceId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage(message));
            } catch (Exception e) {
                // Log l'erreur
                removeSession(deviceId);
            }
        }
    }

    /**
     * SCÉNARIO: Envoyer un message à tous les devices connectés
     *
     * @param message Message JSON
     */
    public void broadcast(String message) {
        for (Map.Entry<UUID, WebSocketSession> entry : sessions.entrySet()) {
            try {
                if (entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(new org.springframework.web.socket.TextMessage(message));
                }
            } catch (Exception e) {
                // Log
            }
        }
    }
}