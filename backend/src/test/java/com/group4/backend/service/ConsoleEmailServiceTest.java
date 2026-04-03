package com.group4.backend.service;
import com.group4.backend.service.email.ConsoleEmailService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConsoleEmailServiceTest {

    private final ConsoleEmailService consoleEmailService = new ConsoleEmailService();

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void redirectSystemOut() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @Test
    void logActive_shouldRunWithoutThrowing() {
        assertThatCode(() -> consoleEmailService.logActive()).doesNotThrowAnyException();
    }

    @Test
    void sendConfirmationEmail_shouldPrintSimulatedEmailToConsole() {
        String email = "user@test.com";
        String link = "http://localhost:5173/confirm?token=abc";

        consoleEmailService.sendConfirmationEmail(email, link);

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(output).as("confirmation header").contains("CONFIRMATION EMAIL (simulated) FOR: " + email),
                () -> assertThat(output).as("confirmation link").contains("Confirm your account: " + link),
                () -> assertThat(output).as("separator line").contains("------------------------------------------------")
        );
    }

    @Test
    void sendPasswordResetEmail_shouldPrintSimulatedEmailToConsole() {
        String email = "user@test.com";
        String resetLink = "http://localhost:5173/reset?token=xyz";

        consoleEmailService.sendPasswordResetEmail(email, resetLink);

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(output).as("password reset header").contains("PASSWORD RESET EMAIL (simulated) FOR: " + email),
                () -> assertThat(output).as("reset link").contains("Reset your password: " + resetLink),
                () -> assertThat(output).as("separator line").contains("------------------------------------------------")
        );
    }

    @Test
    void sendVerificationStatusEmail_approved_shouldPrintApprovedStatus() {
        consoleEmailService.sendVerificationStatusEmail("user@test.com", true, "Looks good");

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(output).as("verification header").contains("VERIFICATION STATUS UPDATE (simulated) FOR: user@test.com"),
                () -> assertThat(output).as("approved status").contains("Status: APPROVED"),
                () -> assertThat(output).as("reason text").contains("Reason: Looks good")
        );
    }

    @Test
    void sendVerificationStatusEmail_rejected_shouldPrintRejectedStatus() {
        consoleEmailService.sendVerificationStatusEmail("user@test.com", false, "Incomplete profile");

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(output).as("rejected status").contains("Status: REJECTED"),
                () -> assertThat(output).as("rejection reason").contains("Reason: Incomplete profile")
        );
    }

    @Test
    void sendVerificationStatusEmail_withNullReason_shouldOmitReasonLine() {
        consoleEmailService.sendVerificationStatusEmail("user@test.com", true, null);

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertThat(output).doesNotContain("Reason:");
    }

    @Test
    void sendVerificationStatusEmail_withEmptyReason_shouldOmitReasonLine() {
        consoleEmailService.sendVerificationStatusEmail("user@test.com", false, "");

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertThat(output).doesNotContain("Reason:");
    }
}
