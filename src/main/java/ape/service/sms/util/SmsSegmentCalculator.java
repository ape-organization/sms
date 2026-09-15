package ape.service.sms.util;

import ape.service.sms.entity.SmsEncodingType;

/**
 * Detects whether a message needs Unicode (UCS-2) encoding - required for Arabic and any
 * character outside the GSM 03.38 7-bit alphabet - or can use the plain GSM-7/English
 * encoding, and computes how many SMS parts the message will be split into.
 *
 * <p>Thresholds below are the exact per-part-count limits used by the SMS gateway:</p>
 * <pre>
 * Unicode/Arabic: 70, 134, 198, 261, 328, 403 characters -> 1..6 parts
 * English:        160, 306, 459, 612, 765, 918 characters -> 1..6 parts
 * </pre>
 */
public final class SmsSegmentCalculator {

    private static final int[] ENGLISH_THRESHOLDS = {160, 306, 459, 612, 765, 918};
    private static final int[] UNICODE_THRESHOLDS = {70, 134, 198, 261, 328, 403};

    // Per-segment size used to extrapolate for messages longer than the last known threshold.
    private static final int ENGLISH_SEGMENT_SIZE = 153;
    private static final int UNICODE_SEGMENT_SIZE = 67;

    // GSM 03.38 default alphabet basic character set (single septet).
    private static final String GSM_BASIC =
            "@\u00a3$\u00a5\u00e8\u00e9\u00f9\u00ec\u00f2\u00c7\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ" +
            " !\"#\u00a4%&'()*+,-./0123456789:;<=>?¡" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿" +
            "abcdefghijklmnopqrstuvwxyzäöñüà";

    // GSM 03.38 extension table (counts as 2 septets, still GSM-7 compatible).
    private static final String GSM_EXTENDED = "^{}\\[~]|\u20ac";

    private SmsSegmentCalculator() {
    }

    /**
     * Returns {@link SmsEncodingType#UNICODE} if the message contains any character outside
     * the GSM-7 alphabet (e.g. Arabic script), otherwise {@link SmsEncodingType#ENGLISH}.
     */
    public static SmsEncodingType detectEncoding(String message) {
        return requiresUnicode(message) ? SmsEncodingType.UNICODE : SmsEncodingType.ENGLISH;
    }

    /**
     * Computes how many SMS parts {@code message} will be split into for the given encoding.
     */
    public static int countParts(String message, SmsEncodingType encoding) {
        int length = message == null ? 0 : message.codePointCount(0, message.length());
        if (length == 0) {
            return 1;
        }

        int[] thresholds = encoding == SmsEncodingType.UNICODE ? UNICODE_THRESHOLDS : ENGLISH_THRESHOLDS;
        for (int i = 0; i < thresholds.length; i++) {
            if (length <= thresholds[i]) {
                return i + 1;
            }
        }

        // Beyond the largest documented threshold: extrapolate using the standard per-segment size.
        int segmentSize = encoding == SmsEncodingType.UNICODE ? UNICODE_SEGMENT_SIZE : ENGLISH_SEGMENT_SIZE;
        int lastThreshold = thresholds[thresholds.length - 1];
        int extraParts = (int) Math.ceil((length - lastThreshold) / (double) segmentSize);
        return thresholds.length + extraParts;
    }

    private static boolean requiresUnicode(String message) {
        if (message == null) {
            return false;
        }
        return message.codePoints().anyMatch(cp -> !isGsm7Compatible(cp));
    }

    private static boolean isGsm7Compatible(int codePoint) {
        if (codePoint > Character.MAX_VALUE) {
            return false;
        }
        char c = (char) codePoint;
        return GSM_BASIC.indexOf(c) >= 0 || GSM_EXTENDED.indexOf(c) >= 0;
    }
}
