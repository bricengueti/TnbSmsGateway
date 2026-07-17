package TNB.SmsGateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message de heartbeat
 * Device → Backend
 */
public record HeartbeatMessage(
        @JsonProperty("timestamp")
        String timestamp,

        @JsonProperty("batteryLevel")
        Integer batteryLevel,

        @JsonProperty("signalStrength")
        Integer signalStrength
) {
    public HeartbeatMessage() {
        this(null, null, null);
    }

    public HeartbeatMessage(String timestamp) {
        this(timestamp, null, null);
    }
}