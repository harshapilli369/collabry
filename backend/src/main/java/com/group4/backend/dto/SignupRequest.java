package com.group4.backend.dto;

import com.group4.backend.model.Role;
import jakarta.validation.constraints.*;
import java.util.Set;

public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one digit"
    )
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private static final Set<Role> ALLOWED_SIGNUP_ROLES = Set.of(Role.BRAND, Role.INFLUENCER);

    public SignupRequest() {
    }

    public SignupRequest(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static boolean isAllowedRole(Role role) {
        return role != null && ALLOWED_SIGNUP_ROLES.contains(role);
    }
}
