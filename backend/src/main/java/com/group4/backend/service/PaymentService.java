package com.group4.backend.service;

import com.group4.backend.dto.PaymentRequest;
import com.group4.backend.dto.PaymentResponse;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.Payment;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.repository.CampaignRepository;
import com.group4.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final int INVOICE_NUMBER_LENGTH = 8;

    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;

    public PaymentService(PaymentRepository paymentRepository, CampaignRepository campaignRepository) {
        this.paymentRepository = paymentRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public PaymentResponse createPayment(Long brandId, PaymentRequest request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!campaign.getUserId().equals(brandId)) {
            throw new IllegalArgumentException("You can only create payments for your own campaigns");
        }

        Payment payment = new Payment();
        payment.setCampaignId(request.getCampaignId());
        payment.setInfluencerId(request.getInfluencerId());
        payment.setBrandId(brandId);
        payment.setMilestoneName(request.getMilestoneName().trim());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDueDate(request.getDueDate());
        payment.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, INVOICE_NUMBER_LENGTH).toUpperCase());
        payment.setNotes(emptyToNull(request.getNotes()));

        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsForInfluencer(Long influencerId) {
        return paymentRepository.findByInfluencerIdOrderByDueDateDesc(influencerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsForCampaign(Long campaignId, Long brandId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!campaign.getUserId().equals(brandId)) {
            throw new IllegalArgumentException("You can only view payments for your own campaigns");
        }

        return paymentRepository.findByCampaignIdOrderByDueDateAsc(campaignId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus, Long brandId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!payment.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("You can only update payments you created");
        }

        payment.setStatus(newStatus);
        if (newStatus == PaymentStatus.PAID) {
            payment.setPaidDate(LocalDate.now());
        }

        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional
    public List<PaymentResponse> getDelayedPayments(Long brandId) {
        // 1. Find and update any newly delayed payments
        paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.PENDING, LocalDate.now())
                .stream()
                .filter(p -> p.getBrandId().equals(brandId))
                .forEach(p -> {
                    p.setStatus(PaymentStatus.DELAYED);
                    paymentRepository.save(p);
                });

        // 2. Return all currently delayed payments for this brand
        return paymentRepository.findByBrandIdAndStatus(brandId, PaymentStatus.DELAYED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getInvoice(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!payment.getInfluencerId().equals(userId) && !payment.getBrandId().equals(userId)) {
            throw new IllegalArgumentException("You do not have access to this invoice");
        }

        return toResponse(payment);
    }

    private static String emptyToNull(String value) {
        if (value == null)
            return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setCampaignId(p.getCampaignId());
        r.setInfluencerId(p.getInfluencerId());
        r.setBrandId(p.getBrandId());
        r.setMilestoneName(p.getMilestoneName());
        r.setAmount(p.getAmount());
        r.setStatus(p.getStatus());
        r.setDueDate(p.getDueDate());
        r.setPaidDate(p.getPaidDate());
        r.setInvoiceNumber(p.getInvoiceNumber());
        r.setNotes(p.getNotes());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());

        // Denormalize campaign name
        campaignRepository.findById(p.getCampaignId())
                .ifPresent(c -> r.setCampaignName(c.getName()));

        return r;
    }
}
