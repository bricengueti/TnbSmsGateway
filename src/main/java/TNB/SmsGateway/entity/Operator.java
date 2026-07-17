package TNB.SmsGateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "operators", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
public class Operator extends BaseAudit {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    public Operator() {
        super();
    }

    public Operator(String code, String displayName, Country country) {
        this();
        this.code = code;
        this.displayName = displayName;
        this.country = country;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
}