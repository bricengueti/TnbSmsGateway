package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.PairingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PairingCodeRepository extends JpaRepository<PairingCode, UUID> {

    // ✅ Recherche directe par hash déterministe (comme ApiKeyRepository.findByKeyHash)
    Optional<PairingCode> findByCodeHash(String codeHash);

    /**
     * Le code de connexion actif d'un utilisateur (non révoqué).
     * Utilisé pour l'affichage dashboard et pour la régénération
     * (révoquer l'ancien avant d'en créer un nouveau).
     */
    @Query("SELECT pc FROM PairingCode pc WHERE pc.user.id = :userId AND pc.revokedAt IS NULL")
    Optional<PairingCode> findActiveByUserId(@Param("userId") UUID userId);
}