package com.group4.backend.service;

import com.group4.backend.dto.RatingRequest;
import com.group4.backend.dto.RatingResponse;
import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InfluencerRating;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.repository.InfluencerRatingRepository;
import com.group4.backend.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private InfluencerRatingRepository ratingRepository;
    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private RatingService ratingService;

    private CollaborationInvitation confirmedInvitation;
    private Long brandId = 10L;
    private Long influencerId = 20L;

    @BeforeEach
    void setUp() {
        confirmedInvitation = new CollaborationInvitation();
        confirmedInvitation.setId(100L);
        confirmedInvitation.setBrandId(brandId);
        confirmedInvitation.setInfluencerId(influencerId);
        confirmedInvitation.setStatus(InvitationStatus.CONFIRMED);
    }

    @Test
    void submitRating_whenInvitationConfirmed_andBrandOwner_returnsResponse() {
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        request.setReview("Great collaboration!");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));
        when(ratingRepository.findByInvitationId(100L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(InfluencerRating.class))).thenAnswer(inv -> {
            InfluencerRating r = inv.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        RatingResponse response = ratingService.submitRating(brandId, request);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getReview()).isEqualTo("Great collaboration!");
        verify(ratingRepository).save(any(InfluencerRating.class));
    }

    @Test
    void submitRating_whenNotBrandOwner_throws() {
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(4);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));

        assertThatThrownBy(() -> ratingService.submitRating(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the brand that collaborated");
    }

    @Test
    void submitRating_whenInvitationNotConfirmed_throws() {
        confirmedInvitation.setStatus(InvitationStatus.ACCEPTED);
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(4);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));

        assertThatThrownBy(() -> ratingService.submitRating(brandId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only rate after the collaboration is completed");
    }

    @Test
    void getAverageRating_whenNoRatings_returnsZero() {
        when(ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)).thenReturn(List.of());
        double avg = ratingService.getAverageRating(influencerId);
        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    void getAverageRating_whenHasRatings_returnsAverage() {
        InfluencerRating r1 = new InfluencerRating();
        r1.setRating(4);
        InfluencerRating r2 = new InfluencerRating();
        r2.setRating(5);
        when(ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)).thenReturn(List.of(r1, r2));
        double avg = ratingService.getAverageRating(influencerId);
        assertThat(avg).isEqualTo(4.5);
    }
}
