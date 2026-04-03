package com.group4.backend.testsupport;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot app for slice tests so {@code @SpringBootTest} does not pick up
 * {@link com.group4.backend.BackendApplication} from the parent package (which would load all controllers).
 */
@SpringBootApplication(scanBasePackages = "com.group4.backend.testsupport")
public class SliceTestApplication {
}
