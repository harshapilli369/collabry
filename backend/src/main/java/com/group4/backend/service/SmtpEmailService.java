package com.group4.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    /** Sender address (set app.mail.from or spring.mail.username). */
    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
        mailSender.send(message);
    }
}
