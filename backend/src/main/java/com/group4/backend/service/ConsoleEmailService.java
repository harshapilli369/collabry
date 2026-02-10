package com.group4.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Used when SMTP is not configured (no spring.mail.host).
 * Set spring.mail.host (and related properties) to send real emails.
 */
@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host", havingValue = "", matchIfMissing = true)
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendConfirmationEmail(String email, String confirmationLinkOrToken) {
        System.out.println("------------------------------------------------");
        System.out.println("CONFIRMATION EMAIL (simulated) FOR: " + email);
        System.out.println("Confirm your account: " + confirmationLinkOrToken);
        System.out.println("------------------------------------------------");
    }
}
