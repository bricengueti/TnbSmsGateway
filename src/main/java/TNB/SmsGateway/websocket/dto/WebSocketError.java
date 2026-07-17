package TNB.SmsGateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message d'erreur WebSocket
 * Backend → Device
 */
public record WebSocketError(
        @JsonProperty("code")
        String code,

        @JsonProperty("message")
        String message,

        @JsonProperty("timestamp")
        String timestamp
) {
    public static WebSocketError invalidToken() {
        return new WebSocketError("AUTH_FAILURE", "Token invalide ou expiré", null);
    }

    public static WebSocketError unknownType(String type) {
        return new WebSocketError("UNKNOWN_TYPE", "Type de message inconnu: " + type, null);
    }

    public static WebSocketError internalError(String message) {
        return new WebSocketError("INTERNAL_ERROR", message, null);
    }
}