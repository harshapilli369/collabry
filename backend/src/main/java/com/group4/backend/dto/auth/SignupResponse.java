package com.group4.backend.dto.auth;

/**
 * Returned after signup when email confirmation is required.
 * User is not created until they click the confirmation link.
 */
public class SignupResponse {

    private String message;

    public SignupResponse() {
    }

    public SignupResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
