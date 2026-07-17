package TNB.SmsGateway.exception.message;

import TNB.SmsGateway.exception.BusinessException;
import java.util.UUID;

public class MessageNotFoundException extends BusinessException {

    public MessageNotFoundException(UUID messageId) {
        super("Message non trouvé: " + messageId, "MESSAGE_NOT_FOUND", 404);
    }
}