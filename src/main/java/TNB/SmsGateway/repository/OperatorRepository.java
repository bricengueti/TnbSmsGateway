package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {

    /**
     * Trouver un opérateur par son code
     */
    Optional<Operator> findByCode(String code);

    /**
     * Trouver tous les opérateurs d'un pays
     */
    List<Operator> findByCountryCode(String countryCode);

    /**
     * Trouver tous les opérateurs d'un pays avec leur pays
     */
    @Query("SELECT o FROM Operator o JOIN FETCH o.country WHERE o.country.code = :countryCode")
    List<Operator> findByCountryCodeWithCountry(@Param("countryCode") String countryCode);

    /**
     * Vérifier si un opérateur appartient à un pays
     */
    @Query("SELECT COUNT(o) > 0 FROM Operator o WHERE o.code = :operatorCode AND o.country.code = :countryCode")
    boolean existsByCodeAndCountryCode(@Param("operatorCode") String operatorCode,
                                       @Param("countryCode") String countryCode);

    /**
     * Trouver un opérateur avec son pays
     */
    @Query("SELECT o FROM Operator o JOIN FETCH o.country WHERE o.code = :code")
    Optional<Operator> findByCodeWithCountry(@Param("code") String code);

    /**
     * Trouver tous les opérateurs d'un pays (triés par nom)
     */
    @Query("SELECT o FROM Operator o WHERE o.country.code = :countryCode ORDER BY o.displayName")
    List<Operator> findByCountryCodeOrderByDisplayName(@Param("countryCode") String countryCode);

    /**
     * Compter les opérateurs par pays
     */
    @Query("SELECT o.country.code, COUNT(o) FROM Operator o GROUP BY o.country.code")
    List<Object[]> countOperatorsByCountry();
}