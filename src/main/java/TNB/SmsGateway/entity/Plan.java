package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
public class Plan extends BaseAudit {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // null = illimité
    @Column(name = "monthly_sms_limit")
    private Integer monthlySmsLimit;

    @Column(name = "price_monthly")
    private BigDecimal priceMonthly;

    // Permet de retirer un pack de la vente sans casser les clients qui l'ont déjà
    @Column(nullable = false)
    private boolean active = true;

    public Plan() {
        super();
    }

    public Plan(String name, String description, Integer monthlySmsLimit, BigDecimal priceMonthly) {
        this();
        this.name = name;
        this.description = description;
        this.monthlySmsLimit = monthlySmsLimit;
        this.priceMonthly = priceMonthly;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMonthlySmsLimit() { return monthlySmsLimit; }
    public void setMonthlySmsLimit(Integer monthlySmsLimit) { this.monthlySmsLimit = monthlySmsLimit; }

    public BigDecimal getPriceMonthly() { return priceMonthly; }
    public void setPriceMonthly(BigDecimal priceMonthly) { this.priceMonthly = priceMonthly; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isUnlimited() { return monthlySmsLimit == null; }
}