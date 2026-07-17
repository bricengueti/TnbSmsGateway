package TNB.SmsGateway.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SecurityUtils {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Hasher un mot de passe avec BCrypt
     */
    public static String hashPassword(String plainText) {
        return passwordEncoder.encode(plainText);
    }

    /**
     * Vérifier un mot de passe avec BCrypt
     */
    public static boolean verifyPassword(String plainText, String hashed) {
        return passwordEncoder.matches(plainText, hashed);
    }

    /**
     * Hasher avec BCrypt (alias)
     */
    public static String hash(String text) {
        return BCrypt.hashpw(text, BCrypt.gensalt());
    }

    /**
     * Vérifier avec BCrypt (alias)
     */
    public static boolean verify(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }

    /**
     * Hasher avec SHA-256
     */
    public static String hashSha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Hasher avec SHA-256 et Base64
     */
    public static String hashSha256Base64(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Convertir bytes en hexadécimal
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Générer un token sécurisé (UUID)
     */
    public static String generateSecureToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Générer un secret aléatoire
     */
    public static String generateSecret(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}