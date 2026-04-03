package com.group4.backend.config.mail;

import com.group4.backend.testsupport.SliceTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = SliceTestApplication.class)
@Import(RelaxedMailConfig.class)
@TestPropertySource(properties = {
        "app.mail.ssl.relaxed=true",
        "spring.mail.host=smtp.example.com",
        "spring.mail.port=465",
        "spring.mail.username=u",
        "spring.mail.password=p"
})
class RelaxedMailConfigSslPortTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void javaMailSenderUsesSslWhenPortIs465() {
        JavaMailSenderImpl impl = (JavaMailSenderImpl) javaMailSender;
        Properties props = impl.getJavaMailProperties();
        assertAll(
                () -> assertThat(impl.getHost()).as("mail host").isEqualTo("smtp.example.com"),
                () -> assertThat(impl.getPort()).as("mail port").isEqualTo(465),
                () -> assertThat(props.getProperty("mail.smtp.ssl.enable")).as("SSL enabled").isEqualTo("true"),
                () -> assertThat(props.getProperty("mail.smtp.starttls.enable")).as("STARTTLS not set").isNull(),
                () -> assertThat(props.get("mail.smtp.ssl.socketFactory")).as("SSL socket factory present").isNotNull()
        );
    }
}
