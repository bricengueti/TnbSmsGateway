package TNB.SmsGateway.entity;

import jakarta.persistence.*;
import java.time.Instant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "pairing_code")
    private String pairingCode;

    @Column(name = "pairing_code_expires_at")
    private Instant pairingCodeExpiresAt;

    @Column(name = "paired_at")
    private Instant pairedAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    // ===== RELATION AVEC DeviceSim =====
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceSim> sims = new ArrayList<>();

    // ===== CONSTRUCTEURS =====

    public Device() {
        super();
    }

    public Device(User user, Country country, String label) {
        this();
        this.user = user;
        this.country = country;
        this.label = label;
    }

    // ===== GETTERS & SETTERS =====

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

    public String getPairingCode() { return pairingCode; }
    public void setPairingCode(String pairingCode) { this.pairingCode = pairingCode; }

    public Instant getPairingCodeExpiresAt() { return pairingCodeExpiresAt; }
    public void setPairingCodeExpiresAt(Instant pairingCodeExpiresAt) { this.pairingCodeExpiresAt = pairingCodeExpiresAt; }

    public Instant getPairedAt() { return pairedAt; }
    public void setPairedAt(Instant pairedAt) { this.pairedAt = pairedAt; }

    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(Instant lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }

    public List<DeviceSim> getSims() { return sims; }
    public void setSims(List<DeviceSim> sims) { this.sims = sims; }

    // ===== MÉTHODES UTILITAIRES =====

    public boolean isPairingCodeValid() {
        return pairingCode != null &&
                pairingCodeExpiresAt != null &&
                Instant.now().isBefore(pairingCodeExpiresAt);
    }

    public boolean isOnline() {
        return DeviceStatus.ONLINE.equals(status);
    }

    public void addSim(DeviceSim sim) {
        sims.add(sim);
        sim.setDevice(this);
    }
}
