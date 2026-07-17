package TNB.SmsGateway.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Générer une signature HMAC-SHA256
     */
    public static String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            mac.init(secretKey);
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Vérifier une signature HMAC-SHA256
     */
    public static boolean verifySignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            return false;
        }
        String expectedSignature = generateSignature(payload, secret);
        return expectedSignature.equals(signature);
    }

    /**
     * Générer une signature pour webhook
     */
    public static String generateWebhookSignature(String payload, String webhookSecret) {
        return generateSignature(payload, webhookSecret);
    }

    /**
     * Vérifier une signature de webhook
     */
    public static boolean verifyWebhookSignature(String payload, String signature, String webhookSecret) {
        return verifySignature(payload, signature, webhookSecret);
    }
}