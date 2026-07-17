package TNB.SmsGateway.repository;
import TNB.SmsGateway.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    /**
     * Trouver un pays par son code
     */
    Optional<Country> findByCode(String code);

    /**
     * Trouver un pays par son nom
     */
    Optional<Country> findByName(String name);

    /**
     * Trouver tous les pays avec leurs opérateurs
     */
    @Query("SELECT c FROM Country c LEFT JOIN FETCH c.operators")
    List<Country> findAllWithOperators();

    /**
     * Trouver un pays avec ses opérateurs
     */
    @Query("SELECT c FROM Country c LEFT JOIN FETCH c.operators WHERE c.code = :code")
    Optional<Country> findByCodeWithOperators(@Param("code") String code);

    /**
     * Vérifier si un pays existe
     */
    boolean existsByCode(String code);
}