package TNB.SmsGateway.websocket.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatusUpdateMessage(
        @JsonProperty("messageId")
        String messageId,

        @JsonProperty("status")
        String status,

        @JsonProperty("errorReason")
        String errorReason,

        @JsonProperty("timestamp")
        String timestamp
) {
    public StatusUpdateMessage(String messageId, String status) {
        this(messageId, status, null, null);
    }
}