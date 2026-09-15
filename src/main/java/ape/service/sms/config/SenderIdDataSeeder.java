package ape.service.sms.config;

import ape.service.sms.entity.SenderId;
import ape.service.sms.repository.SenderIdRepository;
import ape.service.sms.sender.WhySmsSender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Seeds the SenderId table on startup in the "dev" profile only, so the
 * /api/sms/send endpoint can be exercised locally without manual DB setup.
 * Adds a sample row wired to the {@link WhySmsSender} provider strategy.
 */
@Configuration
@Profile("dev")
public class SenderIdDataSeeder {

    @Bean
    CommandLineRunner seedSenderIds(SenderIdRepository senderIdRepository) {
        return args -> {
            if (senderIdRepository.count() == 0) {
                SenderId whySmsSenderId = new SenderId();
                whySmsSenderId.setSenderId("ORD-01");
                whySmsSenderId.setSenderIdName("My Order");
                whySmsSenderId.setProvider(WhySmsSender.PROVIDER_KEY);
                whySmsSenderId.setActive(true);
                senderIdRepository.save(whySmsSenderId);
            }
        };
    }
}
