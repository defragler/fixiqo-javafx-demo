package com.defragler.fixiqo.services.enums;

/**
 * Enumeration representing the supported verification methods for user registration,
 * account confirmation, password reset, or two-factor authentication in the Fixiqo system.
 *
 * <p>These types determine the channel through which a one-time verification code
 * or confirmation link will be sent to the user.</p>
 *
 * <p>Each verification type is associated with a specific notification mechanism:
 * <ul>
 *     <li>{@link #EMAIL} — via email (most common, reliable, and user-friendly)</li>
 *     <li>{@link #PHONE} — via SMS (useful for users without email access or for higher security)</li>
 * </ul></p>
 *
 * <p>Future extensions could include: PUSH_NOTIFICATION, TELEGRAM, WHATSAPP, etc.</p>
 */
public enum VerificationType {
    EMAIL,
    PHONE
}
