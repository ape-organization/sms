package ape.service.sms.repository;

import ape.service.sms.entity.SenderName;
import ape.service.sms.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
    @Query("select coalesce(sum(m.smsPartCount), 0) from SmsMessage m "
            + "where m.senderName = :senderName "
            + "and (:from is null or m.createdAt >= :from) "
            + "and (:to is null or m.createdAt < :to)")
    long sumSmsPartCountBySenderNameAndCreatedAtBetween(@Param("senderName") SenderName senderName,
                                                         @Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);
}
