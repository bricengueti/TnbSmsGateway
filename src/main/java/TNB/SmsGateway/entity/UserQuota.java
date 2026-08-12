package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

    // Pertinent pour type=POOL — quota TOTAL pour toute la période (pas de
    // reset mensuel : consommé jusqu'à épuisement ou expiration du pack).
    @Column(name = "sms_quota")
    private Integer smsQuota;

    @Column(name = "sms_sent_in_period", nullable = false)
    private Integer smsSentInPeriod = 0;

    // Pertinent pour type=PERSONAL — nombre de devices autorisés.
    @Column(name = "quantity_devices")
    private Integer quantityDevices;

    // Commun aux deux types — date de fin de validité du pack, calculée à
    // l'assignation comme (maintenant + plan.validityMonths mois). Après
    // cette date, le quota est considéré expiré même s'il reste du solde.
    @Column(name = "period_ends_at")
    private Instant periodEndsAt;

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

    public Integer getSmsQuota() { return smsQuota; }
    public void setSmsQuota(Integer smsQuota) { this.smsQuota = smsQuota; }

    public Integer getSmsSentInPeriod() { return smsSentInPeriod; }
    public void setSmsSentInPeriod(Integer smsSentInPeriod) { this.smsSentInPeriod = smsSentInPeriod; }

    public Integer getQuantityDevices() { return quantityDevices; }
    public void setQuantityDevices(Integer quantityDevices) { this.quantityDevices = quantityDevices; }

    public Instant getPeriodEndsAt() { return periodEndsAt; }
    public void setPeriodEndsAt(Instant periodEndsAt) { this.periodEndsAt = periodEndsAt; }

    /** true si le pack est arrivé au bout de sa durée de validité (validityMonths écoulés). */
    public boolean isExpired() {
        return periodEndsAt != null && Instant.now().isAfter(periodEndsAt);
    }

    /**
     * unlimited=true → toujours OK.
     * Sinon : il faut à la fois du solde restant ET ne pas être expiré —
     * un pack expiré bloque l'envoi même s'il reste du solde SMS non
     * consommé (le solde non utilisé n'est pas reporté après expiration).
     */
    public boolean hasRemainingQuota() {
        if (unlimited) return true;
        if (isExpired()) return false;
        if (smsQuota == null) return false;
        return smsSentInPeriod < smsQuota;
    }

    public void incrementUsage() {
        this.smsSentInPeriod = (this.smsSentInPeriod == null ? 0 : this.smsSentInPeriod) + 1;
    }

    /**
     * Démarre une nouvelle période à partir d'un pack fraîchement assigné :
     * réinitialise le compteur consommé et recalcule la date d'expiration
     * à (maintenant + plan.validityMonths mois calendaires). Remplace
     * l'ancien resetForNewPeriod() basé sur le changement de mois civil.
     */
    public void startNewPeriod(Plan plan) {
        this.smsSentInPeriod = 0;
        this.periodEndsAt = plan.getValidityMonths() != null
                ? ZonedDateTime.now(ZoneOffset.UTC).plusMonths(plan.getValidityMonths()).toInstant()
                : null;
    }
}
