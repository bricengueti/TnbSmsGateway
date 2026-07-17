package TNB.SmsGateway.websocket.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("success")
        boolean success,

        @JsonProperty("message")
        String message,

        @JsonProperty("deviceId")
        String deviceId,

        @JsonProperty("status")
        String status
) {
    public static AuthResponse success(String deviceId, String status) {
        return new AuthResponse(true, "Authentifié avec succès", deviceId, status);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null, null);
    }
}