package ape.service.sms.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SenderName {
    PINKY_AURA("PinkyAura");

    private final String value;

    SenderName(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static SenderName fromValue(String value) {
        return Arrays.stream(values())
                .filter(senderName -> senderName.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported senderName: " + value));
    }
}
