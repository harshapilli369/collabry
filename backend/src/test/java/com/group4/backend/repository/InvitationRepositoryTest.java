package com.group4.backend.repository;

import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class InvitationRepositoryTest {

    @Autowired
    private InvitationRepository invitationRepository;

    @Test
    void shouldSaveInvitation() {
        CollaborationInvitation inv = new CollaborationInvitation();
        inv.setCampaignId(1L);
        inv.setInfluencerId(2L);
        inv.setBrandId(3L);
        inv.setStatus(InvitationStatus.PENDING);
        inv.setBrandMessage("Join our campaign");

        CollaborationInvitation saved = invitationRepository.save(inv);
        invitationRepository.flush();

        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getCampaignId()).isEqualTo(1L),
                () -> assertThat(saved.getInfluencerId()).isEqualTo(2L),
                () -> assertThat(saved.getBrandId()).isEqualTo(3L),
                () -> assertThat(saved.getStatus()).isEqualTo(InvitationStatus.PENDING),
                () -> assertThat(saved.getBrandMessage()).isEqualTo("Join our campaign"),
                () -> assertThat(saved.getCreatedAt()).isNotNull()
        );

        CollaborationInvitation found = invitationRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getInfluencerId()).isEqualTo(2L);
    }

    @Test
    void findByInfluencerIdOrderByCreatedAtDesc_returnsCorrectOrder() {
        long campaignId = 10L;
        long influencerId = 20L;
        long brandId = 30L;

        CollaborationInvitation first = createInvitation(campaignId, influencerId, brandId, InvitationStatus.PENDING);
        invitationRepository.saveAndFlush(first);

        CollaborationInvitation second = createInvitation(campaignId + 1, influencerId, brandId, InvitationStatus.PENDING);
        invitationRepository.saveAndFlush(second);

        List<CollaborationInvitation> list = invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId);
        assertAll(
                () -> assertThat(list).hasSize(2),
                () -> assertThat(list.get(0).getId()).isEqualTo(second.getId()),
                () -> assertThat(list.get(1).getId()).isEqualTo(first.getId())
        );
    }

    @Test
    void findByCampaignIdAndInfluencerId_returnsOptional() {
        long campaignId = 100L;
        long influencerId = 200L;
        long brandId = 300L;

        assertThat(invitationRepository.findByCampaignIdAndInfluencerId(campaignId, influencerId)).isEmpty();

        CollaborationInvitation inv = createInvitation(campaignId, influencerId, brandId, InvitationStatus.PENDING);
        invitationRepository.saveAndFlush(inv);

        Optional<CollaborationInvitation> found = invitationRepository.findByCampaignIdAndInfluencerId(campaignId, influencerId);
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getInfluencerId()).isEqualTo(influencerId),
                () -> assertThat(found.get().getCampaignId()).isEqualTo(campaignId)
        );
    }

    @Test
    void findByInfluencerIdAndStatusIn_returnsOnlyMatchingStatuses() {
        long influencerId = 40L;
        long campaignId = 1L;
        long brandId = 2L;

        invitationRepository.saveAndFlush(createInvitation(campaignId++, influencerId, brandId, InvitationStatus.PENDING));
        invitationRepository.saveAndFlush(createInvitation(campaignId++, influencerId, brandId, InvitationStatus.ACCEPTED));
        invitationRepository.saveAndFlush(createInvitation(campaignId++, influencerId, brandId, InvitationStatus.REJECTED));
        invitationRepository.saveAndFlush(createInvitation(campaignId++, influencerId, brandId, InvitationStatus.CONFIRMED));

        List<CollaborationInvitation> acceptedOrConfirmed = invitationRepository.findByInfluencerIdAndStatusIn(
                influencerId,
                List.of(InvitationStatus.ACCEPTED, InvitationStatus.CONFIRMED)
        );
        assertAll(
                () -> assertThat(acceptedOrConfirmed).hasSize(2),
                () -> assertThat(acceptedOrConfirmed).extracting(CollaborationInvitation::getStatus)
                        .containsExactlyInAnyOrder(InvitationStatus.ACCEPTED, InvitationStatus.CONFIRMED)
        );
    }

    private static CollaborationInvitation createInvitation(long campaignId, long influencerId, long brandId, InvitationStatus status) {
        CollaborationInvitation inv = new CollaborationInvitation();
        inv.setCampaignId(campaignId);
        inv.setInfluencerId(influencerId);
        inv.setBrandId(brandId);
        inv.setStatus(status);
        return inv;
    }
}
