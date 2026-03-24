package com.group4.backend.config;

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

@SpringBootTest(classes = SliceTestApplication.class)
@Import(RelaxedMailConfig.class)
@TestPropertySource(properties = {
        "app.mail.ssl.relaxed=true",
        "spring.mail.host=smtp.example.com",
        "spring.mail.port=587",
        "spring.mail.username=",
        "spring.mail.password="
})
class RelaxedMailConfigStartTlsTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void javaMailSenderUsesStartTlsWhenPortIsNot465() {
        JavaMailSenderImpl impl = (JavaMailSenderImpl) javaMailSender;
        assertThat(impl.getPort()).isEqualTo(587);
        Properties props = impl.getJavaMailProperties();
        assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
        assertThat(props.getProperty("mail.smtp.ssl.enable")).isNull();
    }
}
