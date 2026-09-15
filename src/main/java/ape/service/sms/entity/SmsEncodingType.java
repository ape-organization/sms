package ape.service.sms.entity;

/**
 * Character encoding detected for a message, which drives the SMS part-count thresholds.
 * ENGLISH corresponds to the GSM-7 character set, UNICODE covers Arabic and any other
 * characters outside that set.
 */
public enum SmsEncodingType {
    ENGLISH,
    UNICODE
}
