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

    /** Durée de validité du pack en mois (ex: 1, 3, 12). Toujours renseignée. */
    @Column(name = "validity_months", nullable = false)
    private Integer validityMonths;

    /** Plafond de devices simultanés — pertinent uniquement pour type=PERSONAL. Null = illimité. */
    @Column(name = "quantity_devices")
    private Integer quantityDevices;

    /** Quota total de SMS pour toute la période (pas de reset mensuel) —
     * pertinent uniquement pour type=POOL. Null = illimité. */
    @Column(name = "quantity_sms")
    private Integer quantitySms;

    @Column(name = "price")
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public Plan() { super(); }

    public Plan(String name, String description, PlanType type, Integer validityMonths,
                Integer quantityDevices, Integer quantitySms, BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.validityMonths = validityMonths;
        this.quantityDevices = quantityDevices;
        this.quantitySms = quantitySms;
        this.price = price;
        this.active = active;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PlanType getType() { return type; }
    public void setType(PlanType type) { this.type = type; }

    public Integer getValidityMonths() { return validityMonths; }
    public void setValidityMonths(Integer validityMonths) { this.validityMonths = validityMonths; }

    public Integer getQuantityDevices() { return quantityDevices; }
    public void setQuantityDevices(Integer quantityDevices) { this.quantityDevices = quantityDevices; }

    public Integer getQuantitySms() { return quantitySms; }
    public void setQuantitySms(Integer quantitySms) { this.quantitySms = quantitySms; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
