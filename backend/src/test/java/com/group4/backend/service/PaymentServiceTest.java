package com.group4.backend.service;
import com.group4.backend.service.payment.PaymentService;

import com.group4.backend.dto.payment.PaymentRequest;
import com.group4.backend.dto.payment.PaymentResponse;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.Payment;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setUserId(10L);
        campaign.setName("Spring Launch");
    }

    @Test
    void createPayment_shouldCreatePendingPaymentAndNormalizeInput() {
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(22L);
        request.setMilestoneName("  Publish Reel  ");
        request.setAmount(new BigDecimal("250.00"));
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setNotes("   ");

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        PaymentResponse response = paymentService.createPayment(10L, request);

        assertAll(
                () -> assertThat(response.getId()).as("payment id").isEqualTo(99L),
                () -> assertThat(response.getStatus()).as("status").isEqualTo(PaymentStatus.PENDING),
                () -> assertThat(response.getMilestoneName()).as("trimmed milestone name").isEqualTo("Publish Reel"),
                () -> assertThat(response.getNotes()).as("blank notes normalized to null").isNull(),
                () -> assertThat(response.getInvoiceNumber()).as("invoice number prefix").startsWith("INV-"),
                () -> assertThat(response.getCampaignName()).as("campaign name").isEqualTo("Spring Launch")
        );
    }

    @Test
    void createPayment_shouldKeepTrimmedNotesWhenNonEmpty() {
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(22L);
        request.setMilestoneName("Milestone");
        request.setAmount(new BigDecimal("100.00"));
        request.setDueDate(LocalDate.now().plusDays(3));
        request.setNotes("  pay after review  ");

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment(10L, request);

        assertThat(response.getNotes()).isEqualTo("pay after review");
    }

    @Test
    void createPayment_shouldThrowWhenCampaignMissing() {
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(777L);
        request.setInfluencerId(22L);
        request.setMilestoneName("Milestone");
        request.setAmount(new BigDecimal("100.00"));

        when(campaignRepository.findById(777L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campaign not found");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowWhenCampaignOwnedByAnotherBrand() {
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(22L);
        request.setMilestoneName("Milestone");
        request.setAmount(new BigDecimal("100.00"));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> paymentService.createPayment(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("your own campaigns");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentsForInfluencer_shouldMapResponses() {
        Payment p = buildPayment(100L, 1L, 22L, 10L, PaymentStatus.PENDING);
        p.setDueDate(LocalDate.now().plusDays(2));

        when(paymentRepository.findByInfluencerIdOrderByDueDateDesc(22L)).thenReturn(List.of(p));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        List<PaymentResponse> responses = paymentService.getPaymentsForInfluencer(22L);

        assertAll(
                () -> assertThat(responses).as("result size").hasSize(1),
                () -> assertThat(responses.get(0).getCampaignName()).as("campaign name").isEqualTo("Spring Launch"),
                () -> assertThat(responses.get(0).getInfluencerId()).as("influencer id").isEqualTo(22L)
        );
    }

    @Test
    void getPaymentsForCampaign_shouldThrowWhenCampaignMissing() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentsForCampaign(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getPaymentsForCampaign_shouldThrowWhenBrandDoesNotOwnCampaign() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> paymentService.getPaymentsForCampaign(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("your own campaigns");
    }

    @Test
    void getPaymentsForCampaign_shouldReturnMappedResponses() {
        Payment p = buildPayment(200L, 1L, 22L, 10L, PaymentStatus.PENDING);
        p.setDueDate(LocalDate.now().plusDays(1));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(1L)).thenReturn(List.of(p));

        List<PaymentResponse> responses = paymentService.getPaymentsForCampaign(1L, 10L);

        assertAll(
                () -> assertThat(responses).as("result size").hasSize(1),
                () -> assertThat(responses.get(0).getId()).as("payment id").isEqualTo(200L),
                () -> assertThat(responses.get(0).getCampaignName()).as("campaign name").isEqualTo("Spring Launch")
        );
    }

    @Test
    void updatePaymentStatus_shouldThrowWhenPaymentMissing() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updatePaymentStatus(1L, PaymentStatus.PAID, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void updatePaymentStatus_shouldThrowWhenBrandDoesNotOwnPayment() {
        Payment payment = buildPayment(1L, 1L, 22L, 10L, PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.updatePaymentStatus(1L, PaymentStatus.PAID, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("payments you created");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_shouldSetPaidDateWhenStatusPaid() {
        Payment payment = buildPayment(1L, 1L, 22L, 10L, PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        PaymentResponse response = paymentService.updatePaymentStatus(1L, PaymentStatus.PAID, 10L);

        assertAll(
                () -> assertThat(response.getStatus()).as("status").isEqualTo(PaymentStatus.PAID),
                () -> assertThat(response.getPaidDate()).as("paid date").isEqualTo(LocalDate.now())
        );
    }

    @Test
    void updatePaymentStatus_shouldNotSetPaidDateForNonPaidStatus() {
        Payment payment = buildPayment(1L, 1L, 22L, 10L, PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        PaymentResponse response = paymentService.updatePaymentStatus(1L, PaymentStatus.PROCESSING, 10L);

        assertAll(
                () -> assertThat(response.getStatus()).as("status").isEqualTo(PaymentStatus.PROCESSING),
                () -> assertThat(response.getPaidDate()).as("paid date null for non-paid").isNull()
        );
    }

    @Test
    void getDelayedPayments_shouldUpdateOnlyPendingOverdueForRequestedBrand() {
        Payment overdueOwned = buildPayment(1L, 1L, 22L, 10L, PaymentStatus.PENDING);
        overdueOwned.setDueDate(LocalDate.now().minusDays(1));
        Payment delayed = buildPayment(3L, 1L, 22L, 10L, PaymentStatus.DELAYED);

        when(paymentRepository.findByBrandIdAndStatusAndDueDateBefore(eq(10L), eq(PaymentStatus.PENDING), any(LocalDate.class)))
                .thenReturn(List.of(overdueOwned));
        when(paymentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByBrandIdAndStatus(10L, PaymentStatus.DELAYED)).thenReturn(List.of(delayed));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        List<PaymentResponse> responses = paymentService.getDelayedPayments(10L);

        assertAll(
                () -> assertThat(overdueOwned.getStatus()).as("overdue status updated").isEqualTo(PaymentStatus.DELAYED),
                () -> assertThat(responses).as("result size").hasSize(1),
                () -> assertThat(responses.get(0).getStatus()).as("response status").isEqualTo(PaymentStatus.DELAYED)
        );
        verify(paymentRepository).saveAll(anyList());
    }

    @Test
    void getInvoice_shouldThrowWhenPaymentMissing() {
        when(paymentRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getInvoice(44L, 22L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void getInvoice_shouldThrowWhenUserHasNoAccess() {
        Payment payment = buildPayment(44L, 1L, 22L, 10L, PaymentStatus.PENDING);
        when(paymentRepository.findById(44L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getInvoice(44L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not have access");
    }

    @Test
    void getInvoice_shouldAllowInfluencerAndBrandAccess() {
        Payment payment = buildPayment(44L, 1L, 22L, 10L, PaymentStatus.PENDING);
        when(paymentRepository.findById(44L)).thenReturn(Optional.of(payment));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        PaymentResponse influencerResponse = paymentService.getInvoice(44L, 22L);
        PaymentResponse brandResponse = paymentService.getInvoice(44L, 10L);

        assertAll(
                () -> assertThat(influencerResponse.getId()).as("influencer access payment id").isEqualTo(44L),
                () -> assertThat(brandResponse.getId()).as("brand access payment id").isEqualTo(44L),
                () -> assertThat(brandResponse.getCampaignName()).as("campaign name").isEqualTo("Spring Launch")
        );
    }

    private Payment buildPayment(Long id, Long campaignId, Long influencerId, Long brandId, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCampaignId(campaignId);
        payment.setInfluencerId(influencerId);
        payment.setBrandId(brandId);
        payment.setMilestoneName("Milestone");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(status);
        payment.setInvoiceNumber("INV-12345678");
        return payment;
    }
}
