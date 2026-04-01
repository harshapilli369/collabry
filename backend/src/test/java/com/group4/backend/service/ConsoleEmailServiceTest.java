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
                () -> assertThat(output).contains("CONFIRMATION EMAIL (simulated) FOR: " + email),
                () -> assertThat(output).contains("Confirm your account: " + link),
                () -> assertThat(output).contains("------------------------------------------------")
        );
    }

    @Test
    void sendPasswordResetEmail_shouldPrintSimulatedEmailToConsole() {
        String email = "user@test.com";
        String resetLink = "http://localhost:5173/reset?token=xyz";

        consoleEmailService.sendPasswordResetEmail(email, resetLink);

        String output = capturedOut.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertThat(output).contains("PASSWORD RESET EMAIL (simulated) FOR: " + email),
                () -> assertThat(output).contains("Reset your password: " + resetLink),
                () -> assertThat(output).contains("------------------------------------------------")
        );
    }
}
