package com.group4.backend.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * When app.mail.ssl.relaxed=true (local dev only), provides a JavaMailSender that trusts
 * all SSL certificates. Use only to work around "PKIX path building failed" when
 * ssl.trust and port 465 do not help. Never enable in production.
 */
@Configuration
@ConditionalOnProperty(name = "app.mail.ssl.relaxed", havingValue = "true")
public class RelaxedMailConfig {

    private static final int SMTP_SSL_PORT = 465;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    @Primary
    public JavaMailSender javaMailSender() throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        if (port == SMTP_SSL_PORT) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        // Use JavaMail / Angus MailSSLSocketFactory to trust all hosts (avoids PKIX errors)
        Object sslFactory = createTrustAllMailSslSocketFactory();
        props.put("mail.smtp.ssl.socketFactory", sslFactory);

        sender.setJavaMailProperties(props);
        return sender;
    }

    /**
     * Jakarta Mail 2 (Angus) uses {@code org.eclipse.angus.mail.util.MailSSLSocketFactory};
     * older stacks used {@code com.sun.mail.util.MailSSLSocketFactory}.
     */
    private static Object createTrustAllMailSslSocketFactory() throws Exception {
        String[] classNames = {
                "org.eclipse.angus.mail.util.MailSSLSocketFactory",
                "com.sun.mail.util.MailSSLSocketFactory"
        };
        Exception last = null;
        for (String className : classNames) {
            try {
                Object factory = Class.forName(className).getDeclaredConstructor().newInstance();
                factory.getClass().getMethod("setTrustAllHosts", boolean.class).invoke(factory, true);
                return factory;
            } catch (ClassNotFoundException e) {
                last = e;
            }
        }
        if (last != null) {
            throw new IllegalStateException("No MailSSLSocketFactory on classpath (Angus / com.sun.mail)", last);
        }
        throw new IllegalStateException("No MailSSLSocketFactory on classpath");
    }
}
