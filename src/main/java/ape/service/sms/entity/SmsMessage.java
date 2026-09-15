package ape.service.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Log of every SMS send attempt, recording the resolved data and the computed SMS part count.
 */
@Entity
@Table(name = "sms_message")
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_name", nullable = false)
    private SenderName senderName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "encoding", nullable = false)
    private SmsEncodingType encoding;

    @Column(name = "sms_part_count", nullable = false)
    private int smsPartCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SmsStatus status;

    @Lob
    @Column(name = "provider_response")
    private String providerResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public SenderName getSenderName() { return senderName; }
    public void setSenderName(SenderName senderName) { this.senderName = senderName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SmsEncodingType getEncoding() { return encoding; }
    public void setEncoding(SmsEncodingType encoding) { this.encoding = encoding; }

    public int getSmsPartCount() { return smsPartCount; }
    public void setSmsPartCount(int smsPartCount) { this.smsPartCount = smsPartCount; }

    public SmsStatus getStatus() { return status; }
    public void setStatus(SmsStatus status) { this.status = status; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
