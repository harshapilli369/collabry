package com.group4.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards SPA routes to index.html so the ResourceHandler serves the full file
 * (including script tags). Using forward ensures the built index.html is served correctly.
 */
@Controller
@RequestMapping
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
    })
    public String serveSpa() {
        return "forward:/index.html";
    }
}
