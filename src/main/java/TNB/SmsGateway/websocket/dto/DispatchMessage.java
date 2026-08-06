package TNB.SmsGateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DispatchMessage(
        @JsonProperty("messageId")
        String messageId,

        @JsonProperty("SlotIndex")
        String slotIndex,

        @JsonProperty("to")
        String to,

        @JsonProperty("body")
        String body,

        @JsonProperty("priority")
        String priority
) {
    public DispatchMessage(String messageId, String slotIndex, String to, String body) {
        this(messageId, slotIndex ,to, body, "NORMAL");
    }


}