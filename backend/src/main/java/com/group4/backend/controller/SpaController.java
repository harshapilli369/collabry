package com.group4.backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Serves index.html for all SPA routes (/, /login, /signup, etc.).
 * Ensures the React app loads for client-side routing.
 */
@RestController
public class SpaController {

    @GetMapping(value = {
            "/",
            "/login",
            "/signup",
            "/forgot-password",
            "/reset-password",
            "/confirm-email",
            "/influencer/**",
            "/brand/**"
    }, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> serveSpa() throws IOException {
        Resource index = new ClassPathResource("/static/index.html");
        if (!index.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(index);
    }
}
