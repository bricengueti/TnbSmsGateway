package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "webhook_delivery_attempts")
public class WebhookDeliveryAttempt extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "success", nullable = false)
    private Boolean success = false;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    public WebhookDeliveryAttempt() {
        super();
        this.attemptedAt = Instant.now();
    }

    public WebhookDeliveryAttempt(Message message, Integer attemptNumber) {
        this();
        this.message = message;
        this.attemptNumber = attemptNumber;
    }

    // Getters and Setters
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public Instant getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Instant attemptedAt) { this.attemptedAt = attemptedAt; }
}