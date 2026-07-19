package TNB.SmsGateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "device_sims")
public class DeviceSim extends BaseAudit {

    public static final String QUOTA_UNLIMITED = "ILLIMITE";

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @ManyToOne
    @JoinColumn(name = "operator_code", nullable = false)
    private Operator operator;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "daily_sms_sent")
    private Integer dailySmsSent = 0;

    // 🔥 String au lieu d'Integer : "ILLIMITE" ou une valeur numérique ("100", "500", ...)
    @Column(name = "daily_sms_quota")
    private String dailySmsQuota = "100";

    public DeviceSim() {
        super();
    }

    public DeviceSim(Device device, Integer slotIndex, Operator operator) {
        this();
        this.device = device;
        this.slotIndex = slotIndex;
        this.operator = operator;
    }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public Integer getSlotIndex() { return slotIndex; }
    public void setSlotIndex(Integer slotIndex) { this.slotIndex = slotIndex; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getDailySmsSent() { return dailySmsSent; }
    public void setDailySmsSent(Integer dailySmsSent) { this.dailySmsSent = dailySmsSent; }

    public String getDailySmsQuota() { return dailySmsQuota; }
    public void setDailySmsQuota(String dailySmsQuota) { this.dailySmsQuota = dailySmsQuota; }

    // ===== MÉTHODES UTILITAIRES =====

    public boolean isUnlimited() {
        return QUOTA_UNLIMITED.equalsIgnoreCase(dailySmsQuota);
    }

    /**
     * Retourne le quota numérique, ou null si illimité.
     * @throws NumberFormatException si la valeur stockée n'est ni "ILLIMITE" ni un nombre valide
     */
    public Integer getNumericQuota() {
        if (isUnlimited()) return null;
        return Integer.parseInt(dailySmsQuota);
    }

    public boolean hasQuota() {
        if (isUnlimited()) return true;
        try {
            int quota = Integer.parseInt(dailySmsQuota);
            return dailySmsSent < quota;
        } catch (NumberFormatException e) {
            // Valeur corrompue en base : on bloque par sécurité plutôt que de laisser passer sans limite
            return false;
        }
    }

    public void incrementDailySmsSent() {
        this.dailySmsSent++;
    }
}