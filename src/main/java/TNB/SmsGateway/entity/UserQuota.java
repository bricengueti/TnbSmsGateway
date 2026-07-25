package TNB.SmsGateway.entity;

import jakarta.persistence.*;

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
    private Plan plan;   // nullable — historique du dernier pack acheté, informatif

    @Column(nullable = false)
    private boolean unlimited = false;

    // Solde de crédits SMS restants (type=POOL) — décrémenté à chaque envoi, jamais réinitialisé
    @Column(name = "sms_credits_remaining")
    private Integer smsCreditsRemaining = 0;

    // Pertinent pour type=PERSONAL — nombre de devices déjà pairés/autorisés
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

    public Integer getSmsCreditsRemaining() { return smsCreditsRemaining; }
    public void setSmsCreditsRemaining(Integer smsCreditsRemaining) { this.smsCreditsRemaining = smsCreditsRemaining; }

    public Integer getMaxDevices() { return maxDevices; }
    public void setMaxDevices(Integer maxDevices) { this.maxDevices = maxDevices; }

    public boolean hasRemainingCredits() {
        if (unlimited) return true;
        return smsCreditsRemaining != null && smsCreditsRemaining > 0;
    }

    public void consumeCredit() {
        if (!unlimited && smsCreditsRemaining != null) {
            this.smsCreditsRemaining = this.smsCreditsRemaining - 1;
        }
    }

    public void addCredits(int amount) {
        this.smsCreditsRemaining = (this.smsCreditsRemaining == null ? 0 : this.smsCreditsRemaining) + amount;
    }
}