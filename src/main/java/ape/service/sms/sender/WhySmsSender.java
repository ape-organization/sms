package ape.service.sms.sender;

import ape.service.sms.model.WhySmsRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * {@link SmsSender} strategy implementation backed by the WhySMS REST API.
 */
@Component
public class WhySmsSender implements SmsSender {

    public static final String PROVIDER_KEY = "WHYSMS";

    private static final Logger log = LoggerFactory.getLogger(WhySmsSender.class);

    @Value("${app.sms.api-url}")
    private String apiUrl;

    @Value("${app.sms.bearer-token}")
    private String bearerToken;

    private final RestClient restClient = RestClient.builder()
            .requestInterceptor((request, body, execution) -> {
                log.info(">>> {} {}\nHeaders: {}\nBody: {}",
                        request.getMethod(), request.getURI(),
                        request.getHeaders(),
                        new String(body, StandardCharsets.UTF_8));
                return execution.execute(request, body);
            })
            .build();

    @Override
    public SmsSendResult send(String phoneNumber, String senderIdName, String message) {
        String recipient = normalize(phoneNumber);

        WhySmsRequestBody body = WhySmsRequestBody.builder()
                .recipient(recipient)
                .senderId(senderIdName)
                .type("plain")
                .message(message)
                .build();

        try {
            String response = restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + bearerToken)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            log.info("SMS sent to {}. Body: {}", recipient, response);
            return new SmsSendResult(true, response);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", recipient, e.getMessage());
            return new SmsSendResult(false, e.getMessage());
        }
    }

    @Override
    public String getProviderKey() {
        return PROVIDER_KEY;
    }

    private String normalize(String phoneNumber) {
        return phoneNumber.startsWith("+") ? phoneNumber : "+2" + phoneNumber;
    }
}
