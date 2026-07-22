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

    @Column(name = "daily_sms_quota")
    private String dailySmsQuota = "100";

    // ✅ Surcharge optionnelle de la cadence pour cette SIM précise.
    // Si null, on retombe sur les valeurs du Device parent.
    @Column(name = "dispatch_min_delay_sec")
    private Integer dispatchMinDelaySec;

    @Column(name = "dispatch_max_delay_sec")
    private Integer dispatchMaxDelaySec;

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

    public Integer getDispatchMinDelaySec() { return dispatchMinDelaySec; }
    public void setDispatchMinDelaySec(Integer dispatchMinDelaySec) { this.dispatchMinDelaySec = dispatchMinDelaySec; }

    public Integer getDispatchMaxDelaySec() { return dispatchMaxDelaySec; }
    public void setDispatchMaxDelaySec(Integer dispatchMaxDelaySec) { this.dispatchMaxDelaySec = dispatchMaxDelaySec; }

    // ===== MÉTHODES UTILITAIRES =====

    public boolean isUnlimited() {
        return QUOTA_UNLIMITED.equalsIgnoreCase(dailySmsQuota);
    }

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
            return false;
        }
    }

    public void incrementDailySmsSent() {
        this.dailySmsSent++;
    }

    /**
     * ✅ Résout la cadence effective : surcharge de la SIM si définie,
     * sinon celle du Device parent.
     */
    public int resolveEffectiveMinDelaySec() {
        return dispatchMinDelaySec != null ? dispatchMinDelaySec : device.getDispatchMinDelaySec();
    }

    public int resolveEffectiveMaxDelaySec() {
        return dispatchMaxDelaySec != null ? dispatchMaxDelaySec : device.getDispatchMaxDelaySec();
    }
}