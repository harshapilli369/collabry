package com.group4.backend.service.campaign;

import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.CampaignStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public CampaignService(CampaignRepository campaignRepository, UserRepository userRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CampaignResponse create(Long userId, CampaignRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.BRAND) {
            throw new IllegalArgumentException("Only brand users can create campaigns");
        }

        Campaign campaign = new Campaign();
        campaign.setUserId(userId);
        campaign.setName(request.getName().trim());
        campaign.setDescription(emptyToNull(request.getDescription()));
        campaign.setBudgetRange(request.getBudgetRange());
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setCampaignGoal(request.getCampaignGoal());
        campaign.setPreferredContentTypes(emptyToNull(request.getPreferredContentTypes()));
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setNumberOfInfluencers(request.getNumberOfInfluencers());

        campaign = campaignRepository.save(campaign);
        return toResponse(campaign);
    }

    public List<CampaignResponse> findByUserId(Long userId) {
        return campaignRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<CampaignResponse> findById(Long id) {
        return campaignRepository.findById(id).map(this::toResponse);
    }
    
    @Transactional
    public CampaignResponse updateStatus(Long userId, Long campaignId, CampaignStatus newStatus) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the campaign owner can change its status");
        }
        
        CampaignStatus current = campaign.getStatus();
        if (current == CampaignStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot update a cancelled campaign");
        }
        if (current == CampaignStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot update a completed campaign");
        }
        
        // Basic transition rules
        if (newStatus == CampaignStatus.ACTIVE && current != CampaignStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT campaigns can be published (ACTIVE)");
        }
        if (newStatus == CampaignStatus.COMPLETED && current != CampaignStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE campaigns can be marked as COMPLETED");
        }
        
        campaign.setStatus(newStatus);
        campaign = campaignRepository.save(campaign);
        return toResponse(campaign);
    }

    private static String emptyToNull(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private CampaignResponse toResponse(Campaign c) {
        CampaignResponse r = new CampaignResponse();
        r.setId(c.getId());
        r.setUserId(c.getUserId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setBudgetRange(c.getBudgetRange());
        r.setStatus(c.getStatus());
        r.setCampaignGoal(c.getCampaignGoal());
        r.setPreferredContentTypes(c.getPreferredContentTypes());
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setNumberOfInfluencers(c.getNumberOfInfluencers());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
