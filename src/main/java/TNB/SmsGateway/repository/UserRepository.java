package TNB.SmsGateway.repository;

import TNB.SmsGateway.entity.User;
import TNB.SmsGateway.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Trouver un utilisateur par email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouver tous les utilisateurs actifs
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Trouver tous les utilisateurs avec webhook configuré
     */
    @Query("SELECT u FROM User u WHERE u.webhookUrl IS NOT NULL")
    List<User> findUsersWithWebhook();

    /**
     * Trouver un utilisateur par email et status
     */
    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    /**
     * Mettre à jour le status d'un utilisateur
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    void updateStatus(@Param("userId") UUID userId, @Param("status") UserStatus status);

    /**
     * Mettre à jour le webhook URL
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.webhookUrl = :webhookUrl WHERE u.id = :userId")
    void updateWebhookUrl(@Param("userId") UUID userId, @Param("webhookUrl") String webhookUrl);

    /**
     * Mettre à jour le webhook secret
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.webhookSecret = :secret WHERE u.id = :userId")
    void updateWebhookSecret(@Param("userId") UUID userId, @Param("secret") String secret);

    /**
     * Compter les utilisateurs actifs
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
}