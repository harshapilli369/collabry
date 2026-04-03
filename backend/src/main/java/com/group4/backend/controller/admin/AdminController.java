package com.group4.backend.controller.admin;

import com.group4.backend.dto.admin.*;
import com.group4.backend.service.admin.AdminService;
import com.group4.backend.service.user.VerificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;
    private final VerificationService verificationService;

    public AdminController(AdminService adminService, VerificationService verificationService) {
        this.adminService = adminService;
        this.verificationService = verificationService;
    }

    @GetMapping("/verification-requests")
    public ResponseEntity<List<AdminVerificationRequestDto>> listVerificationRequests() {
        String email = getCurrentUserEmail();
        logger.info("Admin listVerificationRequests called by user: {}", email);
        if (!isCurrentUserAdmin()) {
            logger.warn("Access denied for listVerificationRequests: user {} is not an admin", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AdminVerificationRequestDto> requests = verificationService.listPendingRequests();
        logger.info("Returning {} verification requests", requests.size());
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/verification-requests/{id}")
    public ResponseEntity<?> processVerificationRequest(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationProcessRequest request) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            verificationService.processRequest(id, request);
            return ResponseEntity.ok(Map.of("message", "Verification request processed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(adminService.buildDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(adminService.listUsers(PageRequest.of(safePage, safeSize)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(adminService.updateUserStatus(id, request.getActive(), request.getFlagged()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }

    private static boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
