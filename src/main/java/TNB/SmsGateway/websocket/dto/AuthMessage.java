package TNB.SmsGateway.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message d'authentification
 * Device → Backend
 */
public record AuthMessage(
        @JsonProperty("deviceId")
        String deviceId,

        @JsonProperty("secretToken")
        String secretToken
) {}
