package ape.service.sms.sender;

/**
 * Outcome of a single {@link SmsSender#send} call.
 */
public record SmsSendResult(boolean success, String providerResponse) {
}
