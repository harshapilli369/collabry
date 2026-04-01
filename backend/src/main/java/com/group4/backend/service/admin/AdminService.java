package com.group4.backend.service.admin;

import com.group4.backend.dto.admin.AdminActiveCollaborationDto;
import com.group4.backend.dto.admin.AdminDashboardResponse;
import com.group4.backend.dto.admin.AdminRecentSignupDto;
import com.group4.backend.dto.admin.AdminUserPageResponse;
import com.group4.backend.dto.admin.AdminUserSummaryDto;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final int DASHBOARD_LIST_LIMIT = 10;
    private static final Set<Role> SIGNUP_ROLES = Set.of(Role.BRAND, Role.INFLUENCER, Role.USER);

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final InvitationRepository invitationRepository;
    private final PaymentRepository paymentRepository;

    public AdminService(UserRepository userRepository,
                        CampaignRepository campaignRepository,
                        InvitationRepository invitationRepository,
                        PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.campaignRepository = campaignRepository;
        this.invitationRepository = invitationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse buildDashboard() {
        var response = new AdminDashboardResponse();
        response.setBrandCount(userRepository.countByRole(Role.BRAND));
        response.setInfluencerCount(userRepository.countByRole(Role.INFLUENCER));
        response.setCampaignCount(campaignRepository.count());

        var signupPage = userRepository.findByRoleInOrderByCreatedAtDesc(
                SIGNUP_ROLES, PageRequest.of(0, DASHBOARD_LIST_LIMIT));
        response.setRecentSignups(signupPage.getContent().stream()
                .map(this::toRecentSignup)
                .collect(Collectors.toList()));

        var collabPage = invitationRepository.findByStatusOrderByUpdatedAtDesc(
                InvitationStatus.ACCEPTED, PageRequest.of(0, DASHBOARD_LIST_LIMIT));
        response.setActiveCollaborations(collabPage.getContent().stream()
                .map(inv -> {
                    var dto = new AdminActiveCollaborationDto();
                    dto.setInvitationId(inv.getId());
                    dto.setCampaignId(inv.getCampaignId());
                    dto.setBrandId(inv.getBrandId());
                    dto.setInfluencerId(inv.getInfluencerId());
                    dto.setUpdatedAt(inv.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList()));

        Map<String, Long> payments = new HashMap<>();
        for (PaymentStatus s : PaymentStatus.values()) {
            payments.put(s.name(), paymentRepository.countByStatus(s));
        }
        response.setPaymentsByStatus(payments);

        return response;
    }

    private AdminRecentSignupDto toRecentSignup(User u) {
        var dto = new AdminRecentSignupDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setActive(u.isActive());
        dto.setFlagged(u.isFlagged());
        return dto;
    }

    @Transactional(readOnly = true)
    public AdminUserPageResponse listUsers(Pageable pageable) {
        Page<User> page = userRepository.findByRoleNotOrderByCreatedAtDesc(Role.ADMIN, pageable);
        var out = new AdminUserPageResponse();
        out.setContent(page.getContent().stream().map(this::toSummary).collect(Collectors.toList()));
        out.setTotalElements(page.getTotalElements());
        out.setTotalPages(page.getTotalPages());
        out.setNumber(page.getNumber());
        out.setSize(page.getSize());
        return out;
    }

    private AdminUserSummaryDto toSummary(User u) {
        var dto = new AdminUserSummaryDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setActive(u.isActive());
        dto.setFlagged(u.isFlagged());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    @Transactional
    public AdminUserSummaryDto updateUserStatus(Long userId, boolean active, boolean flagged) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot modify admin accounts");
        }
        user.setActive(active);
        user.setFlagged(flagged);
        user = userRepository.save(user);
        return toSummary(user);
    }
}
