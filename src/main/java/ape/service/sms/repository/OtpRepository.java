package ape.service.sms.repository;

import ape.service.sms.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findTopByPhoneAndUsedFalseOrderByExpiresAtDesc(String phone);

    @Modifying
    @Transactional
    @Query("UPDATE OtpRecord o SET o.used = true WHERE o.phone = :phone")
    void invalidateAllForPhone(String phone);
}
