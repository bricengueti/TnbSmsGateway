package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User extends BaseAudit {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    // 🔥 AJOUTER CE CHAMP
    @Column(name = "first_login")
    private Boolean firstLogin = true;

    // Constructeurs
    public User() {
        super();
        this.webhookSecret = java.util.UUID.randomUUID().toString();
        this.firstLogin = true;
    }

    public User(String email) {
        this();
        this.email = email;
    }

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public Boolean getFirstLogin() { return firstLogin; }
    public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }

    // 🔥 Méthode pour marquer le premier login
    public void markFirstLoginDone() {
        this.firstLogin = false;
    }
}
