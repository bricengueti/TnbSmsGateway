package TNB.SmsGateway.websocket.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message générique WebSocket
 */
public record WebSocketMessage(
        @JsonProperty("type")
        String type,

        @JsonProperty("data")
        Object data
) {}