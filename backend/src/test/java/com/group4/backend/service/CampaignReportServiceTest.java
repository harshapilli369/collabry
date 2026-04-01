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
        assertThat(pdfText).startsWith("%PDF-");
        assertThat(bytes.length).isGreaterThan(200);
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

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes)).startsWith("%PDF-");
    }
}
