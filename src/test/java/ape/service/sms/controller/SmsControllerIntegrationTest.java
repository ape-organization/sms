package ape.service.sms.controller;

import ape.service.sms.entity.SenderId;
import ape.service.sms.entity.SenderName;
import ape.service.sms.entity.SmsEncodingType;
import ape.service.sms.entity.SmsMessage;
import ape.service.sms.entity.SmsStatus;
import ape.service.sms.repository.SenderIdRepository;
import ape.service.sms.repository.SmsMessageRepository;
import ape.service.sms.sender.SmsSendResult;
import ape.service.sms.sender.WhySmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * End-to-end test of the contract-first POST /api/sms/send endpoint against an
 * in-memory H2 database. The real {@link WhySmsSender} bean is spied on so its
 * provider key/wiring is exercised for real, but the outbound network call is
 * stubbed out.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class SmsControllerIntegrationTest {

    private static final String SENDER_CODE = "ORD-01";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SenderIdRepository senderIdRepository;

    @Autowired
    private SmsMessageRepository smsMessageRepository;

    @MockitoSpyBean
    private WhySmsSender whySmsSender;

    @BeforeEach
    void setUp() {
        smsMessageRepository.deleteAll();
        senderIdRepository.deleteAll();

        SenderId senderId = new SenderId();
        senderId.setSenderId(SENDER_CODE);
        senderId.setSenderIdName("My Order");
        senderId.setProvider(WhySmsSender.PROVIDER_KEY);
        senderId.setActive(true);
        senderIdRepository.save(senderId);

        doReturn(new SmsSendResult(true, "OK")).when(whySmsSender).send(any(), any(), any());
    }

    @Test
    void sendsSmsAndReplacesSenderNamePlaceholder() {
        Map<String, String> request = Map.of(
                "message", "Hello {senderName}, welcome!",
                "senderId", SENDER_CODE,
                "senderName", "PinkyAura",
                "phoneNumber", "01000000000");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/sms/send", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Hello PinkyAura, welcome!");
        assertThat(response.getBody()).containsEntry("smsPartCount", 1);
        assertThat(response.getBody()).containsEntry("encoding", "ENGLISH");
        assertThat(response.getBody()).containsEntry("status", "SENT");
        assertThat(smsMessageRepository.count()).isEqualTo(1);
    }

    @Test
    void returns404WhenSenderIdIsUnknown() {
        Map<String, String> request = Map.of(
                "message", "test",
                "senderId", "UNKNOWN",
                "senderName", "PinkyAura",
                "phoneNumber", "0100");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/sms/send", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void countsMultiPartUnicodeMessage() {
        String arabicMessage = "مرحبا {senderName}، رمز التحقق الخاص بك هو 123456 وهو صالح لمدة خمس دقائق فقط "
                + "يرجى عدم مشاركته مع أي شخص آخر لضمان أمان حسابك الشخصي دائما";
        Map<String, String> request = Map.of(
                "message", arabicMessage,
                "senderId", SENDER_CODE,
                "senderName", "PinkyAura",
                "phoneNumber", "0100");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/sms/send", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("encoding", "UNICODE");
        assertThat(response.getBody()).containsEntry("smsPartCount", 3);
    }

    @Test
    void returns400WhenSenderNameIsInvalid() {
        Map<String, String> request = Map.of(
                "message", "Hello {senderName}",
                "senderId", SENDER_CODE,
                "senderName", "Acme Store",
                "phoneNumber", "01000000000");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/sms/send", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returnsSmsPartCountTotalForSenderName() {
        Map<String, String> firstRequest = Map.of(
                "message", "Hello {senderName}",
                "senderId", SENDER_CODE,
                "senderName", "PinkyAura",
                "phoneNumber", "01000000000");
        Map<String, String> secondRequest = Map.of(
                "message", "Your OTP is 123456",
                "senderId", SENDER_CODE,
                "senderName", "PinkyAura",
                "phoneNumber", "01000000000");

        restTemplate.postForEntity("/api/sms/send", firstRequest, Map.class);
        restTemplate.postForEntity("/api/sms/send", secondRequest, Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total",
                Map.class,
                "PinkyAura");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("senderName", "PinkyAura");
        assertThat(response.getBody()).containsEntry("totalSmsPartCount", 2);
    }

    @Test
    void defaultsToCurrentMonthWhenNoDatesProvided() {
        persistMessage(LocalDate.now().atTime(10, 0), 1);
        persistMessage(LocalDate.now().minusMonths(1).atTime(10, 0), 5);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total",
                Map.class,
                "PinkyAura");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("totalSmsPartCount", 1);
    }

    @Test
    void restrictsToProvidedStartAndEndDate() {
        persistMessage(LocalDate.now().withDayOfMonth(1).atTime(10, 0), 2);
        persistMessage(LocalDate.now().minusMonths(1).atTime(10, 0), 7);

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total?startDate={start}&endDate={end}",
                Map.class,
                "PinkyAura", startDate, endDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("totalSmsPartCount", 2);
    }

    @Test
    void returnsAllDataFromStartDateOnwardsWhenOnlyStartDateProvided() {
        LocalDate today = LocalDate.now();
        persistMessage(today.minusDays(10).atTime(9, 0), 100);
        persistMessage(today.minusDays(2).atTime(9, 0), 3);
        persistMessage(today.plusDays(5).atTime(9, 0), 4);

        LocalDate startDate = today.minusDays(3);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total?startDate={start}",
                Map.class,
                "PinkyAura", startDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("totalSmsPartCount", 7);
    }

    @Test
    void returnsAllDataUpToEndDateWhenOnlyEndDateProvided() {
        LocalDate today = LocalDate.now();
        persistMessage(today.minusDays(30).atTime(9, 0), 3);
        persistMessage(today.minusDays(2).atTime(9, 0), 4);
        persistMessage(today.plusDays(2).atTime(9, 0), 100);

        LocalDate endDate = today.minusDays(1);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total?endDate={end}",
                Map.class,
                "PinkyAura", endDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("totalSmsPartCount", 7);
    }

    @Test
    void returns400WhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/sms/sender-names/{senderName}/sms-part-count-total?startDate={start}&endDate={end}",
                Map.class,
                "PinkyAura", startDate, endDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void persistMessage(LocalDateTime createdAt, int smsPartCount) {
        SmsMessage message = new SmsMessage();
        message.setSenderId(SENDER_CODE);
        message.setSenderName(SenderName.PINKY_AURA);
        message.setPhoneNumber("01000000000");
        message.setMessage("test");
        message.setEncoding(SmsEncodingType.ENGLISH);
        message.setSmsPartCount(smsPartCount);
        message.setStatus(SmsStatus.SENT);
        message.setCreatedAt(createdAt);
        smsMessageRepository.save(message);
    }
}
