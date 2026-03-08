package com.group4.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Primary
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    /** Sender address (set app.mail.from or spring.mail.username). */
    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void logActive() {
        log.info("Email: using SMTP (host={}). Confirmation and password-reset emails will be sent.", mailHost);
    }

    @Override
    public void sendConfirmationEmail(String email, String confirmationLinkOrToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject("Confirm your Collabry account");
        message.setText(
                "Welcome to Collabry!\n\n" +
                "Please confirm your email by clicking the link below:\n\n" +
                confirmationLinkOrToken + "\n\n" +
                "If you did not create an account, you can ignore this email.\n\n" +
                "— The Collabry Team"
        );
        try {
            mailSender.send(message);
            log.info("Confirmation email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}: {}", email, e.getMessage());
            throw e;
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject("Reset your Collabry password");
        message.setText(
                "You requested a password reset for your Collabry account.\n\n" +
                "Click the link below to set a new password:\n\n" +
                resetLink + "\n\n" +
                "This link expires in 1 hour. If you did not request a reset, you can ignore this email.\n\n" +
                "— The Collabry Team"
        );
        try {
            mailSender.send(message);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw e;
        }
    }
}
