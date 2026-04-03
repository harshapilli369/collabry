package com.group4.backend.service;
import com.group4.backend.service.email.SmtpEmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setUp() {
        smtpEmailService = new SmtpEmailService(mailSender);
        ReflectionTestUtils.setField(smtpEmailService, "mailHost", "smtp.example.com");
        ReflectionTestUtils.setField(smtpEmailService, "fromAddress", "noreply@collabry.test");
    }

    @Test
    void logActive_shouldLogWithoutThrowing() {
        assertThatCode(() -> smtpEmailService.logActive()).doesNotThrowAnyException();
    }

    @Test
    void sendConfirmationEmail_shouldSendMessageWithFromSubjectAndBody() {
        String link = "http://localhost:5173/confirm?token=abc123";

        smtpEmailService.sendConfirmationEmail("user@test.com", link);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertAll(
                () -> assertThat(msg.getFrom()).as("from address").isEqualTo("noreply@collabry.test"),
                () -> assertThat(msg.getTo()).as("to address").containsExactly("user@test.com"),
                () -> assertThat(msg.getSubject()).as("subject").isEqualTo("Confirm your Collabry account"),
                () -> assertThat(msg.getText()).as("welcome text").contains("Welcome to Collabry"),
                () -> assertThat(msg.getText()).as("confirmation link").contains(link)
        );
    }

    @Test
    void sendConfirmationEmail_shouldOmitFromWhenBlank() {
        ReflectionTestUtils.setField(smtpEmailService, "fromAddress", "   ");

        smtpEmailService.sendConfirmationEmail("user@test.com", "http://link");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isNull();
    }

    @Test
    void sendConfirmationEmail_shouldPropagateSendFailure() {
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> smtpEmailService.sendConfirmationEmail("user@test.com", "http://link"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP down");
    }

    @Test
    void sendPasswordResetEmail_shouldSendMessageWithFromSubjectAndBody() {
        String resetLink = "http://localhost:5173/reset?token=xyz";

        smtpEmailService.sendPasswordResetEmail("user@test.com", resetLink);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertAll(
                () -> assertThat(msg.getFrom()).as("from address").isEqualTo("noreply@collabry.test"),
                () -> assertThat(msg.getTo()).as("to address").containsExactly("user@test.com"),
                () -> assertThat(msg.getSubject()).as("subject").isEqualTo("Reset your Collabry password"),
                () -> assertThat(msg.getText()).as("reset text").contains("password reset"),
                () -> assertThat(msg.getText()).as("reset link").contains(resetLink)
        );
    }

    @Test
    void sendPasswordResetEmail_shouldOmitFromWhenNull() {
        ReflectionTestUtils.setField(smtpEmailService, "fromAddress", null);

        smtpEmailService.sendPasswordResetEmail("user@test.com", "http://reset");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isNull();
    }

    @Test
    void sendPasswordResetEmail_shouldPropagateSendFailure() {
        doThrow(new IllegalStateException("connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> smtpEmailService.sendPasswordResetEmail("user@test.com", "http://reset"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("connection refused");
    }

    @Test
    void sendVerificationStatusEmail_approved_shouldSendApprovedMessage() {
        smtpEmailService.sendVerificationStatusEmail("user@test.com", true, "Great profile");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertAll(
                () -> assertThat(msg.getFrom()).as("from address").isEqualTo("noreply@collabry.test"),
                () -> assertThat(msg.getTo()).as("to address").containsExactly("user@test.com"),
                () -> assertThat(msg.getSubject()).as("subject").isEqualTo("Collabry Verification Status: Approved"),
                () -> assertThat(msg.getText()).as("approved text").contains("approved"),
                () -> assertThat(msg.getText()).as("reason").contains("Great profile"),
                () -> assertThat(msg.getText()).as("premium features text").contains("premium features")
        );
    }

    @Test
    void sendVerificationStatusEmail_rejected_shouldSendRejectedMessage() {
        smtpEmailService.sendVerificationStatusEmail("user@test.com", false, "Incomplete");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertAll(
                () -> assertThat(msg.getSubject()).as("subject").isEqualTo("Collabry Verification Status: Rejected"),
                () -> assertThat(msg.getText()).as("rejected text").contains("rejected"),
                () -> assertThat(msg.getText()).as("rejection reason").contains("Incomplete"),
                () -> assertThat(msg.getText()).as("retry suggestion").contains("try requesting verification again")
        );
    }

    @Test
    void sendVerificationStatusEmail_withNullReason_shouldOmitReasonLine() {
        smtpEmailService.sendVerificationStatusEmail("user@test.com", true, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).doesNotContain("Reason:");
    }

    @Test
    void sendVerificationStatusEmail_withEmptyReason_shouldOmitReasonLine() {
        smtpEmailService.sendVerificationStatusEmail("user@test.com", false, "   ");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).doesNotContain("Reason:");
    }

    @Test
    void sendVerificationStatusEmail_shouldOmitFromWhenBlank() {
        ReflectionTestUtils.setField(smtpEmailService, "fromAddress", "");

        smtpEmailService.sendVerificationStatusEmail("user@test.com", true, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isNull();
    }

    @Test
    void sendVerificationStatusEmail_shouldNotThrowOnSendFailure() {
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> smtpEmailService.sendVerificationStatusEmail("user@test.com", true, null))
                .doesNotThrowAnyException();
    }
}
