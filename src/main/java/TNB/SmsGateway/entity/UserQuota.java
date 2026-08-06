package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_quotas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "type"})
})
public class UserQuota extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType type;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;   // nullable — dernier pack assigné, informatif

    @Column(nullable = false)
    private boolean unlimited = false;

    // Pertinent pour type=POOL — limite + compteur remis à zéro chaque mois
    @Column(name = "monthly_limit")
    private Integer monthlyLimit;

    @Column(name = "sms_sent_this_month", nullable = false)
    private Integer smsSentThisMonth = 0;

    @Column(name = "reset_at")
    private Instant resetAt;

    // Pertinent pour type=PERSONAL — nombre de devices autorisés
    @Column(name = "max_devices")
    private Integer maxDevices;

    public UserQuota() { super(); }

    public UserQuota(User user, PlanType type) {
        this();
        this.user = user;
        this.type = type;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PlanType getType() { return type; }
    public void setType(PlanType type) { this.type = type; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public boolean isUnlimited() { return unlimited; }
    public void setUnlimited(boolean unlimited) { this.unlimited = unlimited; }

    public Integer getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(Integer monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public Integer getSmsSentThisMonth() { return smsSentThisMonth; }
    public void setSmsSentThisMonth(Integer smsSentThisMonth) { this.smsSentThisMonth = smsSentThisMonth; }

    public Instant getResetAt() { return resetAt; }
    public void setResetAt(Instant resetAt) { this.resetAt = resetAt; }

    public Integer getMaxDevices() { return maxDevices; }
    public void setMaxDevices(Integer maxDevices) { this.maxDevices = maxDevices; }

    public boolean hasRemainingQuota() {
        if (unlimited) return true;
        if (monthlyLimit == null) return false;
        return smsSentThisMonth < monthlyLimit;
    }

    public void incrementUsage() {
        this.smsSentThisMonth = (this.smsSentThisMonth == null ? 0 : this.smsSentThisMonth) + 1;
    }

    public void resetForNewPeriod(Instant nextResetAt) {
        this.smsSentThisMonth = 0;
        this.resetAt = nextResetAt;
    }
}