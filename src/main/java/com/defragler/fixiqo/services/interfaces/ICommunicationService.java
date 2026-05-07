package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.services.enums.*;

/**
 * Interface defining the contract for sending one-time verification codes (and potentially other notifications)
 * via different communication channels in the Fixiqo application.
 *
 * <p>This service serves as a thin abstraction layer over concrete delivery mechanisms:
 * <ul>
 *     <li>email sending (SMTP, SendGrid, etc.)</li>
 *     <li>SMS sending (Twilio, Nexmo/Vonage, etc.)</li>
 *     <li>future extensions: push notifications, messengers (Telegram, Viber, WhatsApp)</li>
 * </ul></p>
 *
 * <p>Main responsibility: deliver short-lived verification codes securely and reliably.
 * Actual code generation, storage, expiration and attempt limiting are handled by
 * {@link IVerificationService} — this interface focuses purely on the delivery step.</p>
 *
 * <p><strong>Security considerations for implementations:</strong>
 * <ul>
 *     <li>never log the full code or sensitive content</li>
 *     <li>use secure transport (TLS for email, encrypted channels for SMS)</li>
 *     <li>handle rate limiting and error reporting gracefully</li>
 *     <li>provide meaningful exceptions for delivery failures</li>
 * </ul></p>
 */
public interface ICommunicationService {
    /**
     * Sends a verification code to the specified target using the chosen communication channel.
     *
     * <p>Implementations should:
     * <ul>
     *     <li>format the message appropriately for the channel (e.g. "Your Fixiqo code: 123456")</li>
     *     <li>include service branding, expiration info if needed, and support contacts</li>
     *     <li>handle delivery errors (invalid target, provider outage, etc.)</li>
     *     <li>not block the calling thread for too long (use async sending where possible)</li>
     * </ul></p>
     *
     * <p>This method should be idempotent for the same code/target — repeated calls with the same
     * parameters should not result in multiple messages (or at least respect rate limits).</p>
     *
     * @param target the destination address: email or phone number
     * @param code   the verification code to send (usually 4–8 digits)
     * @param type   communication channel ({@link VerificationType#EMAIL} or {@link VerificationType#PHONE})
     * @throws ServiceException if sending fails (invalid target, provider error, rate limit, etc.)
     */
    void sendVerificationCode(String target, String code, VerificationType type);
}
