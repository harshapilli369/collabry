package com.group4.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Used when SMTP is not configured (no spring.mail.host).
 * Set spring.mail.host (and related properties) to send real emails.
 */
@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host", havingValue = "", matchIfMissing = true)
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

    @PostConstruct
    public void logActive() {
        log.warn("Email: SMTP not configured (no spring.mail.host). Confirmation links will only be printed in backend logs, not sent by email.");
    }

    @Override
    public void sendConfirmationEmail(String email, String confirmationLinkOrToken) {
        System.out.println("------------------------------------------------");
        System.out.println("CONFIRMATION EMAIL (simulated) FOR: " + email);
        System.out.println("Confirm your account: " + confirmationLinkOrToken);
        System.out.println("------------------------------------------------");
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        System.out.println("------------------------------------------------");
        System.out.println("PASSWORD RESET EMAIL (simulated) FOR: " + email);
        System.out.println("Reset your password: " + resetLink);
        System.out.println("------------------------------------------------");
    }
}
