package TNB.SmsGateway.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class RandomUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Générer un code OTP à 6 chiffres
     */
    public static String generateOtp() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Générer un pairing code à 6 chiffres
     */
    public static String generatePairingCode() {
        return generateOtp();
    }

    /**
     * Générer un UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Générer un UUID sans tirets
     */
    public static String generateUUIDWithoutDashes() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Générer une chaîne aléatoire
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Générer une chaîne aléatoire (chiffres uniquement)
     */
    public static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Générer un secret pour webhook
     */
    public static String generateWebhookSecret() {
        return generateRandomString(32);
    }

    /**
     * Générer une clé API
     */
    public static String generateApiKey() {
        return "tnb_live_" + generateRandomString(20);
    }

    /**
     * Générer une clé API de test
     */
    public static String generateTestApiKey() {
        return "tnb_test_" + generateRandomString(20);
    }
}