package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "provider_reference", nullable = false, unique = true)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal amount;

    public PurchaseOrder() { super(); }

    public PurchaseOrder(User user, Plan plan, String providerName, BigDecimal amount) {
        this();
        this.user = user;
        this.plan = plan;
        this.providerName = providerName;
        this.amount = amount;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}