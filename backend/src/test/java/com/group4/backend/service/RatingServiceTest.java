package com.group4.backend.service;
import com.group4.backend.service.profile.RatingService;

import com.group4.backend.dto.rating.RatingRequest;
import com.group4.backend.dto.rating.RatingResponse;
import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InfluencerRating;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.repository.profile.InfluencerRatingRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

        assertAll(
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.getRating()).as("rating").isEqualTo(5),
                () -> assertThat(response.getReview()).as("review").isEqualTo("Great collaboration!")
        );
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
        confirmedInvitation.setStatus(InvitationStatus.PENDING);
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(4);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));

        assertThatThrownBy(() -> ratingService.submitRating(brandId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only rate after the collaboration is completed");
    }

    @Test
    void submitRating_whenInvitationNotFound_throws() {
        RatingRequest request = new RatingRequest();
        request.setInvitationId(999L);
        request.setRating(5);
        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.submitRating(brandId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_whenInvitationAccepted_succeeds() {
        confirmedInvitation.setStatus(InvitationStatus.ACCEPTED);
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(4);
        request.setReview("Good work");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));
        when(ratingRepository.findByInvitationId(100L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(InfluencerRating.class))).thenAnswer(inv -> {
            InfluencerRating r = inv.getArgument(0);
            r.setId(2L);
            r.setCreatedAt(Instant.now());
            return r;
        });

        RatingResponse response = ratingService.submitRating(brandId, request);

        assertAll(
                () -> assertThat(response.getRating()).as("rating").isEqualTo(4),
                () -> assertThat(response.getReview()).as("review").isEqualTo("Good work")
        );
    }

    @Test
    void submitRating_trimsReviewAndSetsNullWhenBlank() {
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        request.setReview("   ");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));
        when(ratingRepository.findByInvitationId(100L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(InfluencerRating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.submitRating(brandId, request);

        assertThat(response.getReview()).isNull();
    }

    @Test
    void submitRating_whenReviewNull_savesNullReview() {
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        request.setReview(null);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));
        when(ratingRepository.findByInvitationId(100L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(InfluencerRating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.submitRating(brandId, request);

        assertThat(response.getReview()).isNull();
    }

    @Test
    void submitRating_whenExistingRatingForInvitation_updatesSameEntity() {
        InfluencerRating existing = new InfluencerRating();
        existing.setId(50L);
        existing.setInvitationId(100L);
        existing.setRating(3);
        existing.setReview("Old");

        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        request.setReview("Updated review");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(confirmedInvitation));
        when(ratingRepository.findByInvitationId(100L)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(InfluencerRating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.submitRating(brandId, request);

        assertAll(
                () -> assertThat(response.getRating()).as("updated rating").isEqualTo(5),
                () -> assertThat(response.getReview()).as("updated review").isEqualTo("Updated review"),
                () -> assertThat(existing.getId()).as("reused entity id").isEqualTo(50L)
        );
    }

    @Test
    void getRatingsForInfluencer_returnsMappedListNewestFirst() {
        InfluencerRating r1 = new InfluencerRating();
        r1.setId(1L);
        r1.setInvitationId(100L);
        r1.setBrandId(brandId);
        r1.setInfluencerId(influencerId);
        r1.setRating(5);
        r1.setReview("A");
        r1.setCreatedAt(Instant.parse("2025-01-02T10:00:00Z"));

        when(ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)).thenReturn(List.of(r1));

        List<RatingResponse> list = ratingService.getRatingsForInfluencer(influencerId);

        assertAll(
                () -> assertThat(list).as("result size").hasSize(1),
                () -> assertThat(list.get(0).getId()).as("rating id").isEqualTo(1L),
                () -> assertThat(list.get(0).getInvitationId()).as("invitation id").isEqualTo(100L),
                () -> assertThat(list.get(0).getRating()).as("rating value").isEqualTo(5),
                () -> assertThat(list.get(0).getReview()).as("review text").isEqualTo("A")
        );
    }

    @Test
    void getRecentReviews_limitsResults() {
        InfluencerRating r1 = ratingWithRating(5);
        InfluencerRating r2 = ratingWithRating(4);
        InfluencerRating r3 = ratingWithRating(3);
        when(ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId))
                .thenReturn(List.of(r1, r2, r3));

        List<RatingResponse> list = ratingService.getRecentReviews(influencerId, 2);

        assertAll(
                () -> assertThat(list).as("limited result size").hasSize(2),
                () -> assertThat(list.get(0).getRating()).as("first rating").isEqualTo(5),
                () -> assertThat(list.get(1).getRating()).as("second rating").isEqualTo(4)
        );
    }

    private static InfluencerRating ratingWithRating(int value) {
        InfluencerRating r = new InfluencerRating();
        r.setRating(value);
        return r;
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
