package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
public class Plan extends BaseAudit {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType type;

    // Pertinent pour type=POOL : limite mensuelle de SMS (null = illimité)
    @Column(name = "monthly_sms_limit")
    private Integer monthlySmsLimit;

    // Pertinent pour type=PERSONAL : nombre de devices autorisés (null = illimité)
    @Column(name = "max_devices")
    private Integer maxDevices;

    @Column(name = "price")
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public Plan() { super(); }

    public Plan(String name, String description, PlanType type,
                Integer monthlySmsLimit, Integer maxDevices, BigDecimal price) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.monthlySmsLimit = monthlySmsLimit;
        this.maxDevices = maxDevices;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PlanType getType() { return type; }
    public void setType(PlanType type) { this.type = type; }

    public Integer getMonthlySmsLimit() { return monthlySmsLimit; }
    public void setMonthlySmsLimit(Integer monthlySmsLimit) { this.monthlySmsLimit = monthlySmsLimit; }

    public Integer getMaxDevices() { return maxDevices; }
    public void setMaxDevices(Integer maxDevices) { this.maxDevices = maxDevices; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isUnlimitedSms() { return monthlySmsLimit == null; }
    public boolean isUnlimitedDevices() { return maxDevices == null; }
}