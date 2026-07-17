package TNB.SmsGateway.service;

import TNB.SmsGateway.entity.OtpCode;
import TNB.SmsGateway.repository.OtpCodeRepository;
import TNB.SmsGateway.utils.RandomUtils;
import TNB.SmsGateway.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * SERVICE: OtpService
 *
 * DESCRIPTION: Gère le cycle de vie des OTP (One-Time Password)
 * - Génération de codes à 6 chiffres
 * - Hachage pour stockage sécurisé
 * - Envoi par email
 * - Validation et vérification
 * - Gestion des tentatives et expiration
 *
 * SCÉNARIOS:
 * 1. Demande OTP: génération + envoi email
 * 2. Vérification OTP: comparaison du code haché
 * 3. Expiration: code invalide après 5 minutes
 * 4. Trop de tentatives: blocage après 5 échecs
 */
@Service
public class OtpService {


    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int MAX_ATTEMPTS = 5;

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    public OtpService(OtpCodeRepository otpCodeRepository, JavaMailSender mailSender) {
        this.otpCodeRepository = otpCodeRepository;
        this.mailSender = mailSender;
    }

    /**
     * SCÉNARIO: Générer et envoyer un OTP par email
     * ÉTAPES:
     * 1. Supprimer les anciens OTPs non utilisés pour cet email
     * 2. Générer un code à 6 chiffres
     * 3. Hacher le code (BCrypt)
     * 4. Stocker avec expiration +5min
     * 5. Envoyer l'email avec le code
     *
     * @param email Email de destination
     */
    @Transactional
    public void generateAndSendOtp(String email) {
        // 1. Invalider les anciens OTPs non utilisés
        otpCodeRepository.deleteByEmail(email);

        // 2. Générer un OTP à 6 chiffres
        String otpCode = RandomUtils.generateOtp();
        String hashedCode = SecurityUtils.hash(otpCode);

        // 3. Créer l'entité
        OtpCode otp = new OtpCode();
        otp.setEmail(email);
        otp.setCodeHash(hashedCode);
        otp.setExpiresAt(Instant.now().plusSeconds(OTP_EXPIRY_SECONDS));
        otp.setAttempts(0);

        otpCodeRepository.save(otp);
        // 🔥 AFFICHER L'OTP DANS LES LOGS (temporaire)
        log.info("========================================");
        log.info("🔐 OTP pour {} : {}", email, otpCode);
        log.info("========================================");
        // 4. Envoyer l'email
//        sendOtpEmail(email, otpCode);
    }

    /**
     * Envoyer l'OTP par email
     */
    private void sendOtpEmail(String email, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Votre code OTP TNB SMS Gateway");
        message.setText(String.format("""
                Bonjour,
                
                Votre code OTP est : %s
                
                Ce code est valable 5 minutes.
                
                Si vous n'avez pas demandé ce code, ignorez cet email.
                
                Cordialement,
                L'équipe TNB SMS Gateway
                """, otpCode));
        mailSender.send(message);
    }

    /**
     * SCÉNARIO: Trouver un OTP valide pour un email
     *
     * @param email Email de l'utilisateur
     * @return OTP le plus récent non vérifié
     */
    public Optional<OtpCode> findValidOtp(String email) {
        return otpCodeRepository.findLatestByEmail(email);
    }

    /**
     * SCÉNARIO: Vérifier un code OTP (comparaison hash)
     *
     * @param otpCode Entité OTP
     * @param code Code saisi par l'utilisateur
     * @return true si le code correspond
     */
    public boolean verifyCode(OtpCode otpCode, String code) {
        return SecurityUtils.verify(code, otpCode.getCodeHash());
    }

    /**
     * SCÉNARIO: Incrémenter le compteur de tentatives
     *
     * @param otpCode Entité OTP
     */
    @Transactional
    public void incrementAttempts(OtpCode otpCode) {
        otpCode.incrementAttempts();
        otpCodeRepository.save(otpCode);
    }

    /**
     * SCÉNARIO: Marquer l'OTP comme vérifié
     *
     * @param otpCode Entité OTP
     */
    @Transactional
    public void markAsVerified(OtpCode otpCode) {
        otpCode.setVerifiedAt(Instant.now());
        otpCodeRepository.save(otpCode);
    }

    /**
     * SCÉNARIO: Vérifier le rate limit (3 requêtes / 10 min)
     *
     * @param email Email de l'utilisateur
     * @return true si le rate limit est dépassé
     */
    public boolean isRateLimitExceeded(String email) {
        Instant tenMinutesAgo = Instant.now().minusSeconds(600);
        long count = otpCodeRepository.countAttemptsSince(email, tenMinutesAgo);
        return count >= 3;
    }

    /**
     * SCÉNARIO: Nettoyer les OTPs expirés (tâche planifiée)
     */
    @Transactional
    public void cleanExpiredOtps() {
        otpCodeRepository.deleteExpiredOtps(Instant.now());
    }
}