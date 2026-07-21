package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pairing_codes")
public class PairingCode extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ✅ Hash déterministe via SecurityUtils.hash (même approche que
    // ApiKey.keyHash) — permet une recherche indexée directe en base,
    // contrairement au BCrypt utilisé pour Device.secretTokenHash.
    @Column(name = "code_hash", nullable = false, unique = true)
    private String codeHash;

    // Préfixe non sensible affiché en clair dans le dashboard
    @Column(name = "code_prefix", nullable = false)
    private String codePrefix;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public PairingCode() {
        super();
    }

    public PairingCode(User user, String codeHash, String codePrefix) {
        this();
        this.user = user;
        this.codeHash = codeHash;
        this.codePrefix = codePrefix;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public String getCodePrefix() { return codePrefix; }
    public void setCodePrefix(String codePrefix) { this.codePrefix = codePrefix; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isValid() {
        return !isRevoked();
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }
}