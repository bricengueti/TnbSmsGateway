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
     * ⚠️ Dangereux depuis la migration vers des codes génériques (ORANGE, MTN...) :
     * le même code existe désormais dans plusieurs pays, donc cette méthode peut
     * lever NonUniqueResultException. Préférer findByCodeAndCountryCode.
     */
    @Deprecated
    Optional<Operator> findByCode(String code);

    List<Operator> findByCountryCode(String countryCode);

    @Query("SELECT o FROM Operator o JOIN FETCH o.country WHERE o.country.code = :countryCode")
    List<Operator> findByCountryCodeWithCountry(@Param("countryCode") String countryCode);

    @Query("SELECT COUNT(o) > 0 FROM Operator o WHERE o.code = :operatorCode AND o.country.code = :countryCode")
    boolean existsByCodeAndCountryCode(@Param("operatorCode") String operatorCode,
                                       @Param("countryCode") String countryCode);

    /**
     * 🔥 Nouvelle méthode : résolution non-ambiguë d'un opérateur, code + pays.
     */
    @Query("SELECT o FROM Operator o JOIN FETCH o.country WHERE o.code = :code AND o.country.code = :countryCode")
    Optional<Operator> findByCodeAndCountryCode(@Param("code") String code,
                                                @Param("countryCode") String countryCode);

    @Query("SELECT o FROM Operator o JOIN FETCH o.country WHERE o.code = :code")
    Optional<Operator> findByCodeWithCountry(@Param("code") String code);

    @Query("SELECT o FROM Operator o WHERE o.country.code = :countryCode ORDER BY o.displayName")
    List<Operator> findByCountryCodeOrderByDisplayName(@Param("countryCode") String countryCode);

    @Query("SELECT o.country.code, COUNT(o) FROM Operator o GROUP BY o.country.code")
    List<Object[]> countOperatorsByCountry();
}