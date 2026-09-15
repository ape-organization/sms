package ape.service.sms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Schema(description = "Request body for sending an SMS via the WhySMS API")
public class WhySmsRequestBody {

    @NotBlank(message = "Recipient phone number must not be blank")
    @Schema(description = "Recipient phone number in international format", example = "+201012345678")
    private String recipient;

    @NotBlank(message = "Sender ID must not be blank")
    @JsonProperty("sender_id")
    @Schema(description = "Registered sender ID shown in the SMS header", example = "MyApp")
    private String senderId;

    @NotBlank(message = "Message type must not be blank")
    @Schema(description = "SMS message type", example = "plain", allowableValues = {"plain", "unicode"})
    private String type;

    @NotBlank(message = "Message content must not be blank")
    @Schema(description = "SMS message body", example = "Your OTP is 123456")
    private String message;

    public WhySmsRequestBody() {}

    public WhySmsRequestBody(String recipient, String senderId, String type, String message) {
        this.recipient = recipient;
        this.senderId = senderId;
        this.type = type;
        this.message = message;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // ── Builder ────────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String recipient;
        private String senderId;
        private String type;
        private String message;

        private Builder() {}

        public Builder recipient(String recipient)  { this.recipient = recipient; return this; }
        public Builder senderId(String senderId)    { this.senderId  = senderId;  return this; }
        public Builder type(String type)            { this.type      = type;      return this; }
        public Builder message(String message)      { this.message   = message;   return this; }

        public WhySmsRequestBody build() {
            return new WhySmsRequestBody(recipient, senderId, type, message);
        }
    }

    // ── Object overrides ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhySmsRequestBody that)) return false;
        return Objects.equals(recipient, that.recipient)
                && Objects.equals(senderId, that.senderId)
                && Objects.equals(type, that.type)
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipient, senderId, type, message);
    }

    @Override
    public String toString() {
        return "WhySmsRequestBody{" +
                "recipient='" + recipient + '\'' +
                ", senderId='" + senderId + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

