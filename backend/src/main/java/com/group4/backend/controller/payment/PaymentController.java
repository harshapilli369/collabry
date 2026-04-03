package com.group4.backend.controller.payment;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.payment.PaymentRequest;
import com.group4.backend.dto.payment.PaymentResponse;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserProvider currentUserProvider;

    public PaymentController(PaymentService paymentService, CurrentUserProvider currentUserProvider) {
        this.paymentService = paymentService;
        this.currentUserProvider = currentUserProvider;
    }

    /** Brand creates a milestone payment for an influencer */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PaymentResponse response = paymentService.createPayment(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Influencer views their payments */
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getPaymentsForInfluencer(user.getId()));
    }

    /** Brand views payments for a specific campaign */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsForCampaign(@PathVariable Long campaignId) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getPaymentsForCampaign(campaignId, user.getId()));
    }

    /** Brand updates payment status (e.g. PENDING â†’ PROCESSING â†’ PAID) */
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }
        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, newStatus, user.getId()));
    }

    /** Download invoice data for a specific payment */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<PaymentResponse> getInvoice(@PathVariable Long id) {
        User user = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(paymentService.getInvoice(id, user.getId()));
    }

    /** Brand views overdue/delayed payments */
    @GetMapping("/delayed")
    public ResponseEntity<List<PaymentResponse>> getDelayedPayments() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getDelayedPayments(user.getId()));
    }
}
