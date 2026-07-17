package TNB.SmsGateway.entity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "countries")
public class Country extends BaseAudit {

    @Id
    @Column(length = 2)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "calling_code", nullable = false)
    private String callingCode;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Operator> operators = new ArrayList<>();

    public Country() {
        super();
    }

    public Country(String code, String name, String callingCode) {
        this();
        this.code = code;
        this.name = name;
        this.callingCode = callingCode;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCallingCode() { return callingCode; }
    public void setCallingCode(String callingCode) { this.callingCode = callingCode; }

    public List<Operator> getOperators() { return operators; }
    public void setOperators(List<Operator> operators) { this.operators = operators; }
}