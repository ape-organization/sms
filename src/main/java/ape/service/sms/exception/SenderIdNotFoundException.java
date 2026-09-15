package ape.service.sms.exception;

/**
 * Thrown when a message is sent using a senderId code that has no matching,
 * active row in the SenderId table.
 */
public class SenderIdNotFoundException extends RuntimeException {

    public SenderIdNotFoundException(String senderId) {
        super("No active SenderId found for code: " + senderId);
    }
}
