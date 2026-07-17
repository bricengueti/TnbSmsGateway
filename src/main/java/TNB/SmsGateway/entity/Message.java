package TNB.SmsGateway.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_user_id", columnList = "user_id"),
        @Index(name = "idx_message_status", columnList = "status"),
        @Index(name = "idx_message_created_at", columnList = "created_at"),
        @Index(name = "idx_message_country_operator", columnList = "country_code, operator_code")
})
public class Message extends BaseAudit {

    // ===== RELATIONS =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_sim_id")
    private DeviceSim deviceSim;

    // ===== CHAMPS =====

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private MessageDirection direction;

    @Column(name = "to_number", nullable = false, length = 20)
    private String toNumber;

    @Column(name = "from_number", length = 20)
    private String fromNumber;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "operator_code", nullable = false, length = 50)
    private String operatorCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessagePriority priority = MessagePriority.NORMAL;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "webhook_delivered_at")
    private Instant webhookDeliveredAt;

    // ===== CONSTRUCTEURS =====

    public Message() {
        super();
    }

    public Message(User user, String toNumber, String body, String countryCode, String operatorCode) {
        this();
        this.user = user;
        this.toNumber = toNumber;
        this.body = body;
        this.countryCode = countryCode;
        this.operatorCode = operatorCode;
        this.direction = MessageDirection.OUTBOUND;
        this.status = MessageStatus.PENDING;
        this.priority = MessagePriority.NORMAL;
        this.attempts = 0;
    }

    public Message(User user, String fromNumber, String body, String countryCode, String operatorCode) {
        this();
        this.user = user;
        this.fromNumber = fromNumber;
        this.body = body;
        this.countryCode = countryCode;
        this.operatorCode = operatorCode;
        this.direction = MessageDirection.INBOUND;
        this.status = MessageStatus.DELIVERED;
        this.priority = MessagePriority.NORMAL;
        this.attempts = 0;
    }

    // ===== GETTERS & SETTERS =====

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public DeviceSim getDeviceSim() { return deviceSim; }
    public void setDeviceSim(DeviceSim deviceSim) { this.deviceSim = deviceSim; }

    public MessageDirection getDirection() { return direction; }
    public void setDirection(MessageDirection direction) { this.direction = direction; }

    public String getToNumber() { return toNumber; }
    public void setToNumber(String toNumber) { this.toNumber = toNumber; }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public MessagePriority getPriority() { return priority; }
    public void setPriority(MessagePriority priority) { this.priority = priority; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Instant dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public Instant getWebhookDeliveredAt() { return webhookDeliveredAt; }
    public void setWebhookDeliveredAt(Instant webhookDeliveredAt) { this.webhookDeliveredAt = webhookDeliveredAt; }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Incrémenter le compteur de tentatives
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Vérifier si le message peut être réessayé
     */
    public boolean canRetry() {
        return this.attempts < 3 && this.status != MessageStatus.DELIVERED;
    }

    /**
     * Vérifier si le message est en attente
     */
    public boolean isPending() {
        return this.status == MessageStatus.PENDING;
    }

    /**
     * Vérifier si le message est dispatché
     */
    public boolean isDispatched() {
        return this.status == MessageStatus.DISPATCHED;
    }

    /**
     * Vérifier si le message est délivré
     */
    public boolean isDelivered() {
        return this.status == MessageStatus.DELIVERED;
    }

    /**
     * Vérifier si le message est un SMS sortant
     */
    public boolean isOutbound() {
        return this.direction == MessageDirection.OUTBOUND;
    }

    /**
     * Vérifier si le message est un SMS entrant
     */
    public boolean isInbound() {
        return this.direction == MessageDirection.INBOUND;
    }

    /**
     * Marquer comme dispatché
     */
    public void markDispatched(Device device, DeviceSim sim) {
        this.status = MessageStatus.DISPATCHED;
        this.device = device;
        this.deviceSim = sim;
        this.dispatchedAt = Instant.now();
    }

    /**
     * Marquer comme envoyé
     */
    public void markSent() {
        this.status = MessageStatus.SENT;
    }

    /**
     * Marquer comme délivré
     */
    public void markDelivered() {
        this.status = MessageStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    /**
     * Marquer comme échoué
     */
    public void markFailed(String reason) {
        this.status = MessageStatus.FAILED;
        this.errorReason = reason;
        this.attempts++;
    }

    /**
     * Marquer comme expiré
     */
    public void markExpired() {
        this.status = MessageStatus.EXPIRED;
        this.errorReason = "Expired after max retries";
    }

    /**
     * Marquer le webhook comme livré
     */
    public void markWebhookDelivered() {
        this.webhookDeliveredAt = Instant.now();
    }
}