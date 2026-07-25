package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "api_key_quotas")
public class ApiKeyQuota extends BaseAudit {

    @OneToOne
    @JoinColumn(name = "api_key_id", nullable = false, unique = true)
    private ApiKey apiKey;

    @Column(name = "unlimited", nullable = false)
    private boolean unlimited = false;

    @Column(name = "monthly_limit")
    private Integer monthlyLimit;

    @Column(name = "sms_sent_this_month", nullable = false)
    private Integer smsSentThisMonth = 0;

    @Column(name = "reset_at")
    private Instant resetAt;
    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;   // nullable — un quota peut aussi être configuré à la main sans plan

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public ApiKeyQuota() {
        super();
    }

    public ApiKeyQuota(ApiKey apiKey, boolean unlimited, Integer monthlyLimit) {
        this();
        this.apiKey = apiKey;
        this.unlimited = unlimited;
        this.monthlyLimit = monthlyLimit;
    }

    public ApiKey getApiKey() { return apiKey; }
    public void setApiKey(ApiKey apiKey) { this.apiKey = apiKey; }

    public boolean isUnlimited() { return unlimited; }
    public void setUnlimited(boolean unlimited) { this.unlimited = unlimited; }

    public Integer getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(Integer monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public Integer getSmsSentThisMonth() { return smsSentThisMonth; }
    public void setSmsSentThisMonth(Integer smsSentThisMonth) { this.smsSentThisMonth = smsSentThisMonth; }

    public Instant getResetAt() { return resetAt; }
    public void setResetAt(Instant resetAt) { this.resetAt = resetAt; }

    public boolean hasRemainingQuota() {
        if (unlimited) return true;
        if (monthlyLimit == null) return false;
        return smsSentThisMonth < monthlyLimit;
    }

    public void increment() {
        this.smsSentThisMonth = (this.smsSentThisMonth == null ? 0 : this.smsSentThisMonth) + 1;
    }

    public void resetForNewPeriod(Instant nextResetAt) {
        this.smsSentThisMonth = 0;
        this.resetAt = nextResetAt;
    }
}