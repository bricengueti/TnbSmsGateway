package TNB.SmsGateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IncomingSmsMessage(
        @JsonProperty("from")
        String from,

        @JsonProperty("body")
        String body,

        @JsonProperty("simSlot")
        Integer simSlot,

        @JsonProperty("receivedAt")
        String receivedAt
) {
    public IncomingSmsMessage(String from, String body, Integer simSlot) {
        this(from, body, simSlot, null);
    }
}