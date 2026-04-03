package com.group4.backend.service;

import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.group4.backend.service.admin.AdminService;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminService adminService;

    private User brandUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        brandUser = new User("b@test.com", "p", Role.BRAND);
        brandUser.setId(1L);
        brandUser.setCreatedAt(Instant.parse("2025-01-01T12:00:00Z"));

        adminUser = new User("a@test.com", "p", Role.ADMIN);
        adminUser.setId(99L);
    }

    @Test
    void buildDashboard_aggregatesCountsAndLists() {
        when(userRepository.countByRole(Role.BRAND)).thenReturn(3L);
        when(userRepository.countByRole(Role.INFLUENCER)).thenReturn(7L);
        when(campaignRepository.count()).thenReturn(12L);

        when(userRepository.findByRoleInOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(brandUser)));

        CollaborationInvitation inv = new CollaborationInvitation();
        inv.setId(10L);
        inv.setCampaignId(5L);
        inv.setBrandId(2L);
        inv.setInfluencerId(3L);
        inv.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));
        when(invitationRepository.findByStatusOrderByUpdatedAtDesc(eq(InvitationStatus.ACCEPTED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(inv)));

        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(4L);
        when(paymentRepository.countByStatus(PaymentStatus.PROCESSING)).thenReturn(1L);
        when(paymentRepository.countByStatus(PaymentStatus.PAID)).thenReturn(8L);
        when(paymentRepository.countByStatus(PaymentStatus.DELAYED)).thenReturn(2L);

        var dash = adminService.buildDashboard();

        assertThat(dash.getBrandCount()).as("brand count").isEqualTo(3);
        assertThat(dash.getInfluencerCount()).as("influencer count").isEqualTo(7);
        assertThat(dash.getCampaignCount()).as("campaign count").isEqualTo(12);
        assertThat(dash.getRecentSignups()).as("recent signups size").hasSize(1);
        assertThat(dash.getRecentSignups().get(0).getEmail()).as("signup email").isEqualTo("b@test.com");
        assertThat(dash.getActiveCollaborations()).as("active collabs size").hasSize(1);
        assertThat(dash.getActiveCollaborations().get(0).getInvitationId()).as("collab invitation id").isEqualTo(10L);
        assertThat(dash.getPaymentsByStatus().get("PENDING")).as("pending payments").isEqualTo(4);
        assertThat(dash.getPaymentsByStatus().get("PAID")).as("paid payments").isEqualTo(8);
    }

    @Test
    void updateUserStatus_rejectsAdminTarget() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminService.updateUserStatus(99L, false, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("admin");
    }

    @Test
    void updateUserStatus_missingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUserStatus(1L, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateUserStatus_persistsBrandUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(brandUser));
        when(userRepository.save(brandUser)).thenReturn(brandUser);

        var out = adminService.updateUserStatus(1L, false, true);

        assertThat(out.isActive()).as("active status").isFalse();
        assertThat(out.isFlagged()).as("flagged status").isTrue();
        verify(userRepository).save(brandUser);
    }

    @Test
    void listUsers_excludesAdminRole() {
        when(userRepository.findByRoleNotOrderByCreatedAtDesc(eq(Role.ADMIN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(brandUser), PageRequest.of(0, 20), 1));

        var page = adminService.listUsers(PageRequest.of(0, 20));

        assertThat(page.getContent()).as("page content size").hasSize(1);
        assertThat(page.getTotalElements()).as("total elements").isEqualTo(1);
    }
}
