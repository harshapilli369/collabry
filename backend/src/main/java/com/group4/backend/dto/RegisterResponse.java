package com.group4.backend.dto;

/**
 * Returned after signup when email confirmation is required.
 * User is not created until they click the confirmation link.
 */
public class RegisterResponse {

    private String message;
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
