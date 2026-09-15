package ape.service.sms.controller;

import ape.service.sms.entity.SmsMessage;
import ape.service.sms.entity.SenderName;
import ape.service.sms.generated.api.SmsApi;
import ape.service.sms.generated.model.SendSmsRequest;
import ape.service.sms.generated.model.SendSmsResponse;
import ape.service.sms.generated.model.SenderSmsPartCountTotalResponse;
import ape.service.sms.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

/**
 * Implements the contract-generated {@link SmsApi} interface. Thin adapter that maps
 * between the OpenAPI contract DTOs and the domain {@link SmsMessage} handled by
 * {@link SmsService}.
 */
@RestController
public class SmsController implements SmsApi {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    public ResponseEntity<SendSmsResponse> sendSms(SendSmsRequest sendSmsRequest) {
        SenderName senderName = SenderName.fromValue(sendSmsRequest.getSenderName().toString());
        SmsMessage logEntry = smsService.sendSms(
                sendSmsRequest.getMessage(),
                sendSmsRequest.getSenderId(),
                senderName,
                sendSmsRequest.getPhoneNumber());

        SendSmsResponse response = new SendSmsResponse()
                .id(logEntry.getId())
                .senderId(logEntry.getSenderId())
                .phoneNumber(logEntry.getPhoneNumber())
                .message(logEntry.getMessage())
                .encoding(SendSmsResponse.EncodingEnum.valueOf(logEntry.getEncoding().name()))
                .smsPartCount(logEntry.getSmsPartCount())
                .status(SendSmsResponse.StatusEnum.valueOf(logEntry.getStatus().name()))
                .createdAt(logEntry.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SenderSmsPartCountTotalResponse> getSmsPartCountTotalBySenderName(String senderName) {
        SenderName senderNameEnum = SenderName.fromValue(senderName);
        long totalSmsPartCount = smsService.getSmsPartCountTotalBySenderName(senderNameEnum);

        SenderSmsPartCountTotalResponse response = new SenderSmsPartCountTotalResponse()
                .senderName(SenderSmsPartCountTotalResponse.SenderNameEnum.fromValue(senderNameEnum.getValue()))
                .totalSmsPartCount(totalSmsPartCount);

        return ResponseEntity.ok(response);
    }
}
