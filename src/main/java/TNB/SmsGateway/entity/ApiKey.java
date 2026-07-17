package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "api_keys")
public class ApiKey extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "key_hash", nullable = false)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false)
    private String keyPrefix;

    @Enumerated(EnumType.STRING)
    private ApiKeyScope scope = ApiKeyScope.FULL;

    private String label;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public ApiKey() {
        super();
    }

    public ApiKey(User user, String keyHash, String keyPrefix) {
        this();
        this.user = user;
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public ApiKeyScope getScope() { return scope; }
    public void setScope(ApiKeyScope scope) { this.scope = scope; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }
}

