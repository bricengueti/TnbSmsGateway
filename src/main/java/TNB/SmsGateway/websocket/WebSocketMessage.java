package TNB.SmsGateway.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CLASSE: WebSocketMessage
 *
 * DESCRIPTION: Message générique pour la communication WebSocket
 * - Encapsule le type et le payload
 * - Utilisé pour tous les échanges entre le backend et le device
 *
 * SCÉNARIOS:
 * 1. Envoi: WebSocketMessage(type, payload) → JSON
 * 2. Réception: JSON → WebSocketMessage(type, payload)
 * 3. Vérification: isType() pour identifier le type
 */
public class WebSocketMessage<T> {

    @JsonProperty("type")
    private String type;

    @JsonProperty("payload")
    private T payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public WebSocketMessage(WebSocketMessageType type, T payload) {
        this.type = type.getValue();
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public boolean isType(WebSocketMessageType type) {
        return this.type != null && this.type.equals(type.getValue());
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", payload=" + payload +
                '}';
    }
}