package com.group4.backend.service;

import com.group4.backend.model.Campaign;
import com.group4.backend.model.CampaignStatus;
import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Payment;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.group4.backend.model.InfluencerProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import com.group4.backend.service.campaign.CampaignReportService;

@ExtendWith(MockitoExtension.class)
class CampaignReportServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @InjectMocks
    private CampaignReportService campaignReportService;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(11L);
        campaign.setUserId(10L);
        campaign.setName("Spring Promo");
        campaign.setStatus(CampaignStatus.ACTIVE);

        CollaborationInvitation invitation = new CollaborationInvitation();
        invitation.setCampaignId(11L);
        invitation.setInfluencerId(20L);
        invitation.setStatus(InvitationStatus.ACCEPTED);

        Payment payment = new Payment();
        payment.setCampaignId(11L);
        payment.setAmount(BigDecimal.valueOf(1200));
        payment.setStatus(PaymentStatus.PAID);

        User influencer = new User();
        influencer.setId(20L);
        influencer.setEmail("influencer@test.com");

        lenient().when(campaignRepository.findById(11L)).thenReturn(Optional.of(campaign));
        lenient().when(invitationRepository.findByCampaignIdOrderByCreatedAtDesc(11L)).thenReturn(List.of(invitation));
        lenient().when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of(payment));
        lenient().when(userRepository.findById(20L)).thenReturn(Optional.of(influencer));
    }

    @Test
    void generateCampaignReportPdf_returnsPdfBytesWithHeader() {
        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdfText = new String(bytes);
        assertThat(pdfText).as("pdf starts with header").startsWith("%PDF-");
        assertThat(bytes.length).as("pdf byte length exceeds minimum").isGreaterThan(200);
    }

    @Test
    void generateCampaignReportPdf_campaignNotOwnedByBrand_throws() {
        assertThatThrownBy(() -> campaignReportService.generateCampaignReportPdf(999L, 11L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @ParameterizedTest
    @EnumSource(CampaignStatus.class)
    void generateCampaignReportPdf_worksForAnyCampaignStatus(CampaignStatus status) {
        campaign.setStatus(status);

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        assertThat(bytes).isNotEmpty();
    }

    @Test
    void generateCampaignReportPdf_withoutPayments_stillGeneratesPdf() {
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        assertThat(bytes).as("pdf bytes not empty").isNotEmpty();
        assertThat(new String(bytes)).as("pdf starts with header").startsWith("%PDF-");
    }

    @Test
    void generateCampaignReportPdf_withoutInvitations_containsNoneLabel() {
        when(invitationRepository.findByCampaignIdOrderByCreatedAtDesc(11L)).thenReturn(List.of());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("None");
    }

    @Test
    void generateCampaignReportPdf_influencerLabelWithProfileAndUser() {
        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(20L);
        profile.setName("Jane Doe");
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).as("pdf contains influencer name").contains("Jane Doe");
        assertThat(pdf).as("pdf contains influencer email").contains("influencer@test.com");
    }

    @Test
    void generateCampaignReportPdf_influencerLabelWithNameOnly() {
        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(20L);
        profile.setName("Solo Name");
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("Solo Name");
    }

    @Test
    void generateCampaignReportPdf_influencerLabelWithEmailOnly() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("influencer@test.com");
    }

    @Test
    void generateCampaignReportPdf_influencerLabelFallsBackToUserId() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("User #20");
    }

    @Test
    void generateCampaignReportPdf_withNullPaymentAmount_treatsAsZero() {
        Payment paymentWithNullAmount = new Payment();
        paymentWithNullAmount.setCampaignId(11L);
        paymentWithNullAmount.setAmount(null);
        paymentWithNullAmount.setStatus(PaymentStatus.PENDING);
        paymentWithNullAmount.setMilestoneName("Milestone 1");
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of(paymentWithNullAmount));

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("Total scheduled: $0");
    }

    @Test
    void generateCampaignReportPdf_withMixedPaidAndPendingPayments_calculatesCorrectly() {
        Payment paid = new Payment();
        paid.setCampaignId(11L);
        paid.setAmount(BigDecimal.valueOf(500));
        paid.setStatus(PaymentStatus.PAID);
        paid.setMilestoneName("Phase 1");

        Payment pending = new Payment();
        pending.setCampaignId(11L);
        pending.setAmount(BigDecimal.valueOf(300));
        pending.setStatus(PaymentStatus.PENDING);
        pending.setMilestoneName("Phase 2");

        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of(paid, pending));

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).as("total scheduled amount").contains("Total scheduled: $800");
        assertThat(pdf).as("total paid amount").contains("Total paid: $500");
        assertThat(pdf).as("outstanding amount").contains("Outstanding: $300");
    }

    @Test
    void generateCampaignReportPdf_withNullCampaignFields_showsDashes() {
        campaign.setDescription(null);
        campaign.setCampaignGoal(null);
        campaign.setBudgetRange(null);
        campaign.setPreferredContentTypes(null);
        campaign.setStartDate(null);
        campaign.setEndDate(null);
        campaign.setNumberOfInfluencers(null);
        when(invitationRepository.findByCampaignIdOrderByCreatedAtDesc(11L)).thenReturn(List.of());
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).as("goal shows dash for null").contains("Goal: -");
        assertThat(pdf).as("budget range shows dash for null").contains("Budget range: -");
    }

    @Test
    void generateCampaignReportPdf_escapesSpecialPdfCharacters() {
        campaign.setName("Campaign (with) \\backslash");
        when(invitationRepository.findByCampaignIdOrderByCreatedAtDesc(11L)).thenReturn(List.of());
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of());

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).as("parentheses escaped").contains("\\(with\\)");
        assertThat(pdf).as("backslash escaped").contains("\\\\backslash");
    }

    @Test
    void generateCampaignReportPdf_withNullMilestoneName_showsDash() {
        Payment payment = new Payment();
        payment.setCampaignId(11L);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMilestoneName(null);
        when(paymentRepository.findByCampaignIdOrderByDueDateAsc(11L)).thenReturn(List.of(payment));

        byte[] bytes = campaignReportService.generateCampaignReportPdf(10L, 11L);

        String pdf = new String(bytes);
        assertThat(pdf).contains("- -");
    }
}
