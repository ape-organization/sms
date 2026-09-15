package ape.service.sms.sender;

/**
 * Strategy interface for dispatching an SMS message to a concrete provider.
 * Each implementation represents one provider integration (Strategy Pattern) and
 * is selected at runtime via its {@link #getProviderKey()}, which must match the
 * {@code provider} column of the {@link ape.service.sms.entity.SenderId} row used.
 */
public interface SmsSender {

    /**
     * Sends {@code message} to {@code phoneNumber} using {@code senderIdName} as the
     * sender identity shown to the recipient.
     */
    SmsSendResult send(String phoneNumber, String senderIdName, String message);

    /**
     * Unique key identifying this provider implementation, e.g. "WHYSMS".
     */
    String getProviderKey();
}
