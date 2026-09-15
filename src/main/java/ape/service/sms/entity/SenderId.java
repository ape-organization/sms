package ape.service.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A registered sender ID that a message can be sent under. Resolves to the actual
 * name shown to recipients (senderIdName) and the provider strategy (provider) used
 * to dispatch the message via {@link ape.service.sms.sender.SmsSender}.
 */
@Entity
@Table(name = "sender_id", uniqueConstraints = @UniqueConstraint(columnNames = "sender_id"))
public class SenderId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false, unique = true)
    private String senderId;

    @Column(name = "sender_id_name", nullable = false)
    private String senderIdName;

    /**
     * Strategy key used to resolve the {@link ape.service.sms.sender.SmsSender}
     * implementation to dispatch the message with, e.g. "WHYSMS".
     */
    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderIdName() { return senderIdName; }
    public void setSenderIdName(String senderIdName) { this.senderIdName = senderIdName; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
