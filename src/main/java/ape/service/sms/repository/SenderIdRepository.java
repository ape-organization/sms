package ape.service.sms.repository;

import ape.service.sms.entity.SenderId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SenderIdRepository extends JpaRepository<SenderId, Long> {

    Optional<SenderId> findBySenderIdAndActiveTrue(String senderId);
}
