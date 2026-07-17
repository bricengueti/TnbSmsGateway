package TNB.SmsGateway.utils;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyUtils {

    private static final String LIVE_PREFIX = "tnb_live_";
    private static final String TEST_PREFIX = "tnb_test_";
    private static final int KEY_LENGTH = 20;

    /**
     * Générer une clé API
     */
    public static String generateApiKey() {
        return LIVE_PREFIX + RandomUtils.generateRandomString(KEY_LENGTH);
    }

    /**
     * Générer une clé API de test
     */
    public static String generateTestApiKey() {
        return TEST_PREFIX + RandomUtils.generateRandomString(KEY_LENGTH);
    }

    /**
     * Extraire le préfixe de la clé
     */
    public static String extractPrefix(String apiKey) {
        if (apiKey == null || apiKey.length() < 15) {
            return null;
        }
        return apiKey.substring(0, 15);
    }

    /**
     * Extraire le suffixe de la clé
     */
    public static String extractSuffix(String apiKey) {
        if (apiKey == null || apiKey.length() < 20) {
            return null;
        }
        return apiKey.substring(apiKey.length() - 5);
    }

    /**
     * Vérifier si la clé est au bon format
     */
    public static boolean isValidFormat(String apiKey) {
        if (apiKey == null) return false;
        return apiKey.startsWith(LIVE_PREFIX) || apiKey.startsWith(TEST_PREFIX);
    }

    /**
     * Vérifier si la clé est une clé de production
     */
    public static boolean isLiveKey(String apiKey) {
        return apiKey != null && apiKey.startsWith(LIVE_PREFIX);
    }

    /**
     * Vérifier si la clé est une clé de test
     */
    public static boolean isTestKey(String apiKey) {
        return apiKey != null && apiKey.startsWith(TEST_PREFIX);
    }

    /**
     * Hasher une clé API
     */
    public static String hashApiKey(String apiKey) {
        return SecurityUtils.hash(apiKey);
    }

    /**
     * Vérifier une clé API
     */
    public static boolean verifyApiKey(String plainApiKey, String hashedApiKey) {
        return SecurityUtils.verify(plainApiKey, hashedApiKey);
    }
}