package com.group4.backend.service;

public interface EmailService {

    /**
     * Send a confirmation email to the user after signup (or log for dev).
     */
    void sendConfirmationEmail(String email, String confirmationLinkOrToken);

    /**
     * Send a password reset email with the reset link (or log for dev).
     */
    void sendPasswordResetEmail(String email, String resetLink);
}
