package ape.service.sms.service;

import java.time.LocalDate;

/**
 * Result of {@link SmsService#getSmsPartCountTotalBySenderName(ape.service.sms.entity.SenderName, LocalDate, LocalDate)}.
 * {@code startDate}/{@code endDate} reflect the effective (resolved) inclusive bounds that were
 * actually used to compute {@code totalSmsPartCount}; either may be {@code null} when the
 * corresponding side of the range is unbounded.
 */
public record SmsPartCountSummary(long totalSmsPartCount, LocalDate startDate, LocalDate endDate) {
}
