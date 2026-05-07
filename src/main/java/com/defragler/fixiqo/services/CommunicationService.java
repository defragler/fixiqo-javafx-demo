package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;
import java.util.concurrent.*;

import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Implementation of the {@link ICommunicationService} interface responsible for delivering
 * verification codes (and potentially other notifications) via email and SMS in the Fixiqo system.
 *
 * <p>Current capabilities:
 * <ul>
 *     <li>Email delivery via Gmail SMTP (STARTTLS, authentication required)</li>
 *     <li>SMS delivery — stub implementation (prints to console)</li>
 * </ul></p>
 *
 * <p><strong>Security & configuration notes:</strong>
 * <ul>
 *     <li>Credentials (EMAIL_FROM, EMAIL_PASSWORD) are hard-coded — in production move to
 *         environment variables, Spring {@code @Value}, or external secrets manager (Vault, AWS Secrets Manager)</li>
 *     <li>Use app password for Gmail (not main account password) — enable 2FA and generate app-specific password</li>
 *     <li>Consider switching to dedicated transactional email service (SendGrid, Mailgun, Amazon SES) for reliability</li>
 *     <li>SMS sending is stubbed — replace with real provider (Twilio, Nexmo/Vonage, MessageBird, etc.)</li>
 * </ul></p>
 *
 * <p>This service is designed to be called by {@link VerificationService} after code generation.</p>
 */
public class CommunicationService implements ICommunicationService {

    // ================= EMAIL CONFIG =================
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private static final String EMAIL_FROM = System.getenv("EMAIL_FROM");
    private static final String EMAIL_PASSWORD = System.getenv("EMAIL_PASSWORD");

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    // =================================================
    /**
     * Sends a verification code to the specified target using the appropriate channel.
     *
     * <p>Delegates to channel-specific methods based on {@link VerificationType}.</p>
     *
     * @param target the destination: email address or phone number
     * @param code   the verification code to deliver
     * @param type   delivery channel (EMAIL or PHONE)
     * @throws ServiceException if sending fails (configuration error, network issue, authentication failure, etc.)
     */
    @Override
    public void sendVerificationCode(String target, String code, VerificationType type) {
        switch (type) {
            case EMAIL -> sendVerificationEmail(target, code);
            case PHONE -> sendVerificationSms(target, code);
        }
    }

    // ---------------- EMAIL ----------------
    private void sendVerificationEmailAsync(String to, String code) {

        CompletableFuture.runAsync(() -> {
            try {
                sendVerificationEmail(to, code);
            } catch (Exception e) {
                throw new ServiceException(ExceptionLevel.ERROR,"Failed to send email");
            }
        }, executor);
    }
    
    /**
     * Sends verification code via email using Gmail SMTP (STARTTLS).
     *
     * <p>Message content:
     * <ul>
     *     <li>Subject: "Fixiqo Verification Code"</li>
     *     <li>Body: code + expiration warning + ignore instruction</li>
     * </ul></p>
     *
     * @param to   recipient email address
     * @param code verification code
     * @throws ServiceException wrapping {@link MessagingException} on failure
     */
    private void sendVerificationEmail(String to, String code) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props,
              new Authenticator() {
                  @Override
                  protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                  }
              });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(
                  Message.RecipientType.TO,
                  InternetAddress.parse(to)
            );
            message.setSubject("Fixiqo - Verification Code");

            message.setText("""
                  Your verification code:

                  %s

                  This code is valid for 5 minutes.
                  If you did not request this code — ignore this message.
                  """.formatted(code));

            Transport.send(message);

        } catch (MessagingException e) {
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to send email");
        }
    }

    // ---------------- SMS ----------------
    /**
     * Stub implementation for SMS delivery — prints code to console for development/testing.
     *
     * <p>In production replace with real SMS provider integration (Twilio, Nexmo, etc.).</p>
     *
     * @param phone phone number
     * @param code  verification code
     */
    private void sendVerificationSms(String phone, String code) {
        System.out.println("[SMS] Code for " + phone + ": " + code);
    }
}
