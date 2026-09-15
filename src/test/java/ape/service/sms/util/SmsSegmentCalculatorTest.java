package ape.service.sms.util;

import ape.service.sms.entity.SmsEncodingType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SmsSegmentCalculatorTest {

    @ParameterizedTest
    @CsvSource({
            "160, 1",
            "306, 2",
            "459, 3",
            "612, 4",
            "765, 5",
            "918, 6"
    })
    void countsEnglishPartsAtEachThreshold(int length, int expectedParts) {
        String message = "A".repeat(length);
        assertThat(SmsSegmentCalculator.detectEncoding(message)).isEqualTo(SmsEncodingType.ENGLISH);
        assertThat(SmsSegmentCalculator.countParts(message, SmsEncodingType.ENGLISH)).isEqualTo(expectedParts);
    }

    @ParameterizedTest
    @CsvSource({
            "70, 1",
            "134, 2",
            "198, 3",
            "261, 4",
            "328, 5",
            "403, 6"
    })
    void countsUnicodePartsAtEachThreshold(int length, int expectedParts) {
        // Arabic letter repeated to force UCS-2/unicode detection.
        String message = "ا".repeat(length);
        assertThat(SmsSegmentCalculator.detectEncoding(message)).isEqualTo(SmsEncodingType.UNICODE);
        assertThat(SmsSegmentCalculator.countParts(message, SmsEncodingType.UNICODE)).isEqualTo(expectedParts);
    }

    @org.junit.jupiter.api.Test
    void oneCharacterOverAThresholdBumpsThePartCount() {
        assertThat(SmsSegmentCalculator.countParts("A".repeat(161), SmsEncodingType.ENGLISH)).isEqualTo(2);
        assertThat(SmsSegmentCalculator.countParts("ا".repeat(71), SmsEncodingType.UNICODE)).isEqualTo(2);
    }

    @org.junit.jupiter.api.Test
    void detectsUnicodeWhenMessageContainsArabic() {
        assertThat(SmsSegmentCalculator.detectEncoding("Hello مرحبا")).isEqualTo(SmsEncodingType.UNICODE);
    }

    @org.junit.jupiter.api.Test
    void detectsEnglishForPlainGsm7Text() {
        assertThat(SmsSegmentCalculator.detectEncoding("Hello, world! 123")).isEqualTo(SmsEncodingType.ENGLISH);
    }
}
