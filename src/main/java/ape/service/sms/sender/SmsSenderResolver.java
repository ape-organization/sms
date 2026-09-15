package ape.service.sms.sender;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the {@link SmsSender} strategy to use for a given provider key
 * (e.g. the {@code provider} column on {@link ape.service.sms.entity.SenderId}).
 * Spring injects every {@link SmsSender} bean here, keyed by its declared provider key,
 * so adding a new provider only requires a new {@link SmsSender} implementation.
 */
@Component
public class SmsSenderResolver {

    private final Map<String, SmsSender> sendersByProvider;

    public SmsSenderResolver(List<SmsSender> senders) {
        this.sendersByProvider = senders.stream()
                .collect(Collectors.toMap(SmsSender::getProviderKey, Function.identity()));
    }

    public SmsSender resolve(String providerKey) {
        SmsSender sender = sendersByProvider.get(providerKey);
        if (sender == null) {
            throw new IllegalStateException("No SmsSender implementation registered for provider: " + providerKey);
        }
        return sender;
    }
}
