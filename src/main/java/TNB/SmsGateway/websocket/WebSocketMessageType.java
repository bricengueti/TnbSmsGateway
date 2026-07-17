package TNB.SmsGateway.websocket;

/**
 * ENUM: WebSocketMessageType
 *
 * DESCRIPTION: Définit tous les types de messages WebSocket
 * - Messages du device vers le backend
 * - Messages du backend vers le device
 *
 * SCÉNARIOS:
 * 1. Device → Backend: AUTH, HEARTBEAT, SMS_STATUS_UPDATE, INCOMING_SMS
 * 2. Backend → Device: AUTH_SUCCESS, AUTH_FAILURE, DISPATCH_SMS
 */
public enum WebSocketMessageType {

    // ===== Device → Backend =====
    AUTH("AUTH"),
    HEARTBEAT("HEARTBEAT"),
    DEVICE_SIMS_REPORT("DEVICE_SIMS_REPORT"),
    SMS_STATUS_UPDATE("SMS_STATUS_UPDATE"),
    INCOMING_SMS("INCOMING_SMS"),

    // ===== Backend → Device =====
    AUTH_SUCCESS("AUTH_SUCCESS"),
    AUTH_FAILURE("AUTH_FAILURE"),
    DISPATCH_SMS("DISPATCH_SMS"),
    REQUEST_SIMS_REPORT("REQUEST_SIMS_REPORT"),
    ERROR("ERROR");

    private final String value;

    WebSocketMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WebSocketMessageType fromValue(String value) {
        for (WebSocketMessageType type : WebSocketMessageType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}