package TNB.SmsGateway.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_email", columnList = "email")
})
public class OtpCode extends BaseAudit {

    @Column(nullable = false)
    private String email;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Integer attempts = 0;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    public OtpCode() {
        super();
        this.expiresAt = Instant.now().plusSeconds(300);
    }

    public OtpCode(String email, String codeHash) {
        this();
        this.email = email;
        this.codeHash = codeHash;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}