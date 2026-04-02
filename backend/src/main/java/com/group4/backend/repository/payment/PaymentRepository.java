package com.group4.backend.repository.payment;

import com.group4.backend.model.Payment;
import com.group4.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInfluencerIdOrderByDueDateDesc(Long influencerId);

    List<Payment> findByCampaignIdOrderByDueDateAsc(Long campaignId);

    List<Payment> findByBrandIdOrderByDueDateDesc(Long brandId);

    List<Payment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);

    List<Payment> findByBrandIdAndStatus(Long brandId, PaymentStatus status);

    List<Payment> findByBrandIdAndStatusAndDueDateBefore(Long brandId, PaymentStatus status, LocalDate date);

    long countByStatus(PaymentStatus status);
}
