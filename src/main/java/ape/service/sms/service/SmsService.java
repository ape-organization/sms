package ape.service.sms.service;

import ape.service.sms.entity.SenderId;
import ape.service.sms.entity.SenderName;
import ape.service.sms.entity.SmsEncodingType;
import ape.service.sms.entity.SmsMessage;
import ape.service.sms.entity.SmsStatus;
import ape.service.sms.exception.SenderIdNotFoundException;
import ape.service.sms.repository.SenderIdRepository;
import ape.service.sms.repository.SmsMessageRepository;
import ape.service.sms.sender.SmsSendResult;
import ape.service.sms.sender.SmsSender;
import ape.service.sms.sender.SmsSenderResolver;
import ape.service.sms.util.SmsSegmentCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Orchestrates sending an SMS for the "send" endpoint:
 * <ol>
 *     <li>Looks up the SenderId row to resolve the real sender name and provider strategy.</li>
 *     <li>Replaces the "{senderName}" placeholder in the message, if present.</li>
 *     <li>Dispatches the message via the {@link SmsSender} strategy for that provider.</li>
 *     <li>Computes the SMS part count and persists a {@link SmsMessage} log entry.</li>
 * </ol>
 */
@Service
public class SmsService {

    private static final String SENDER_NAME_PLACEHOLDER = "{senderName}";

    private final SenderIdRepository senderIdRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsSenderResolver smsSenderResolver;

    public SmsService(SenderIdRepository senderIdRepository,
                       SmsMessageRepository smsMessageRepository,
                       SmsSenderResolver smsSenderResolver) {
        this.senderIdRepository = senderIdRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.smsSenderResolver = smsSenderResolver;
    }

    @Transactional
    public SmsMessage sendSms(String message, String senderId, SenderName senderName, String phoneNumber) {
        SenderId sender = senderIdRepository.findBySenderIdAndActiveTrue(senderId)
                .orElseThrow(() -> new SenderIdNotFoundException(senderId));

        String finalMessage = message.contains(SENDER_NAME_PLACEHOLDER)
                ? message.replace(SENDER_NAME_PLACEHOLDER, senderName.getValue())
                : message;

        SmsEncodingType encoding = SmsSegmentCalculator.detectEncoding(finalMessage);
        int smsPartCount = SmsSegmentCalculator.countParts(finalMessage, encoding);

        SmsSender smsSender = smsSenderResolver.resolve(sender.getProvider());
        SmsSendResult result = smsSender.send(phoneNumber, sender.getSenderIdName(), finalMessage);

        SmsMessage logEntry = new SmsMessage();
        logEntry.setSenderId(senderId);
        logEntry.setSenderName(senderName);
        logEntry.setPhoneNumber(phoneNumber);
        logEntry.setMessage(finalMessage);
        logEntry.setEncoding(encoding);
        logEntry.setSmsPartCount(smsPartCount);
        logEntry.setStatus(result.success() ? SmsStatus.SENT : SmsStatus.FAILED);
        logEntry.setProviderResponse(result.providerResponse());
        logEntry.setCreatedAt(LocalDateTime.now());

        return smsMessageRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public long getSmsPartCountTotalBySenderName(SenderName senderName) {
        return smsMessageRepository.sumSmsPartCountBySenderName(senderName);
    }
}
