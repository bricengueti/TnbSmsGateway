package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices")
public class Device extends BaseAudit {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    private String label;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.DISABLED;

    @Column(name = "secret_token_hash")
    private String secretTokenHash;

    // ❌ Supprimés : pairingCode / pairingCodeExpiresAt
    // Le code de connexion vit désormais au niveau du compte (PairingCode),
    // réutilisable sur plusieurs devices. Le Device n'est créé qu'au moment
    // où ce code est saisi et validé sur le téléphone.

    @Column(name = "paired_at")
    private Instant pairedAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    // ✅ Ajouté : révocation individuelle d'un device sans toucher au
    // code de connexion du compte (qui reste valable pour les autres devices)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceSim> sims = new ArrayList<>();

    public Device() {
        super();
    }

    public Device(User user, Country country, String label) {
        this();
        this.user = user;
        this.country = country;
        this.label = label;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }

    public String getSecretTokenHash() { return secretTokenHash; }
    public void setSecretTokenHash(String secretTokenHash) { this.secretTokenHash = secretTokenHash; }

    public Instant getPairedAt() { return pairedAt; }
    public void setPairedAt(Instant pairedAt) { this.pairedAt = pairedAt; }

    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(Instant lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public List<DeviceSim> getSims() { return sims; }
    public void setSims(List<DeviceSim> sims) { this.sims = sims; }

    public boolean isOnline() {
        return DeviceStatus.ONLINE.equals(status) && !isRevoked();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
        this.status = DeviceStatus.DISABLED;
    }

    public void addSim(DeviceSim sim) {
        sims.add(sim);
        sim.setDevice(this);
    }
}