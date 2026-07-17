package TNB.SmsGateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "device_sims")
public class DeviceSim extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    // ===== RELATION AVEC OPERATOR =====
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
    private Integer dailySmsQuota = 100;

    // ===== CONSTRUCTEURS =====

    public DeviceSim() {
        super();
    }

    public DeviceSim(Device device, Integer slotIndex, Operator operator) {
        this();
        this.device = device;
        this.slotIndex = slotIndex;
        this.operator = operator;
    }

    // ===== GETTERS & SETTERS =====

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

    public Integer getDailySmsQuota() { return dailySmsQuota; }
    public void setDailySmsQuota(Integer dailySmsQuota) { this.dailySmsQuota = dailySmsQuota; }

    // ===== MÉTHODES UTILITAIRES =====

    public boolean hasQuota() {
        return dailySmsSent < dailySmsQuota;
    }

    public void incrementDailySmsSent() {
        this.dailySmsSent++;
    }
}