package com.group4.backend.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Exercises branches in JPA entity lifecycle callbacks and state helpers
 * under {@code com.group4.backend.model} for JaCoCo branch coverage.
 */
class ModelBranchCoverageTest {

    @Nested
    class PendingSignupExpiry {
        @Test
        void isExpired_returnsFalseWhenExpiresAtInFuture() {
            PendingSignup p = new PendingSignup();
            p.setExpiresAt(Instant.now().plusSeconds(3600));

            assertThat(p.isExpired()).isFalse();
        }

        @Test
        void isExpired_returnsTrueWhenExpiresAtInPast() {
            PendingSignup p = new PendingSignup();
            p.setExpiresAt(Instant.now().minusSeconds(60));

            assertThat(p.isExpired()).isTrue();
        }
    }

    @Nested
    class PasswordResetTokenExpiry {
        @Test
        void isExpired_returnsFalseWhenExpiryInFuture() {
            User u = new User("e@test.com", "p", Role.USER);
            PasswordResetToken t = new PasswordResetToken("tok", u);
            t.setExpiryDate(new Date(System.currentTimeMillis() + 60_000));

            assertThat(t.isExpired()).isFalse();
        }

        @Test
        void isExpired_returnsTrueWhenExpiryInPast() {
            User u = new User("e@test.com", "p", Role.USER);
            PasswordResetToken t = new PasswordResetToken("tok", u);
            t.setExpiryDate(new Date(System.currentTimeMillis() - 60_000));

            assertThat(t.isExpired()).isTrue();
        }
    }

    @Nested
    class CampaignCallbacks {
        @Test
        void prePersist_setsCreatedAndUpdatedWhenCreatedAtNull() {
            Campaign c = new Campaign();
            c.prePersist();

            assertThat(c.getCreatedAt()).isNotNull();
            assertThat(c.getUpdatedAt()).isNotNull();
            assertThat(c.getCreatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }

        @Test
        void prePersist_preservesCreatedAtWhenAlreadySet() {
            Campaign c = new Campaign();
            Instant fixed = Instant.parse("2020-06-15T12:00:00Z");
            c.setCreatedAt(fixed);
            c.prePersist();

            assertThat(c.getCreatedAt()).isEqualTo(fixed);
            assertThat(c.getUpdatedAt()).isNotNull();
        }

        @Test
        void preUpdate_refreshesUpdatedAt() {
            Campaign c = new Campaign();
            c.prePersist();
            Instant oldUpdated = c.getUpdatedAt();
            c.preUpdate();

            assertThat(c.getUpdatedAt()).isAfterOrEqualTo(oldUpdated);
        }
    }

    @Nested
    class PaymentCallbacks {
        @Test
        void prePersist_setsTimestampsWhenCreatedAtNull() {
            Payment p = new Payment();
            p.prePersist();

            assertThat(p.getCreatedAt()).isNotNull();
            assertThat(p.getUpdatedAt()).isNotNull();
        }

        @Test
        void prePersist_keepsCreatedAtWhenProvided() {
            Payment p = new Payment();
            Instant fixed = Instant.parse("2021-01-01T00:00:00Z");
            p.setCreatedAt(fixed);
            p.prePersist();

            assertThat(p.getCreatedAt()).isEqualTo(fixed);
        }
    }

    @Nested
    class InfluencerProfileCallbacks {
        @Test
        void prePersist_setsTimestampsWhenCreatedAtNull() {
            InfluencerProfile p = new InfluencerProfile();
            p.prePersist();

            assertThat(p.getCreatedAt()).isNotNull();
            assertThat(p.getUpdatedAt()).isNotNull();
        }

        @Test
        void prePersist_preservesCreatedAtWhenSet() {
            InfluencerProfile p = new InfluencerProfile();
            Instant fixed = Instant.parse("2019-05-05T10:00:00Z");
            p.setCreatedAt(fixed);
            p.prePersist();

            assertThat(p.getCreatedAt()).isEqualTo(fixed);
        }

        @Test
        void preUpdate_updatesUpdatedAt() {
            InfluencerProfile p = new InfluencerProfile();
            p.prePersist();
            Instant before = p.getUpdatedAt();
            p.preUpdate();

            assertThat(p.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class BrandProfileCallbacks {
        @Test
        void prePersist_setsTimestampsWhenCreatedAtNull() {
            BrandProfile b = new BrandProfile();
            b.prePersist();

            assertThat(b.getCreatedAt()).isNotNull();
            assertThat(b.getUpdatedAt()).isNotNull();
        }

        @Test
        void prePersist_preservesCreatedAtWhenSet() {
            BrandProfile b = new BrandProfile();
            Instant fixed = Instant.parse("2022-02-02T02:02:02Z");
            b.setCreatedAt(fixed);
            b.prePersist();

            assertThat(b.getCreatedAt()).isEqualTo(fixed);
        }

        @Test
        void preUpdate_updatesUpdatedAt() {
            BrandProfile b = new BrandProfile();
            b.prePersist();
            Instant before = b.getUpdatedAt();
            b.preUpdate();

            assertThat(b.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class CollaborationInvitationCallbacks {
        @Test
        void prePersist_setsCreatedAndUpdatedWhenCreatedAtNull() {
            CollaborationInvitation inv = new CollaborationInvitation();
            inv.prePersist();

            assertThat(inv.getCreatedAt()).isNotNull();
            assertThat(inv.getUpdatedAt()).isNotNull();
        }

        @Test
        void prePersist_preservesCreatedAtWhenSet() {
            CollaborationInvitation inv = new CollaborationInvitation();
            Instant fixed = Instant.parse("2023-03-03T03:03:03Z");
            inv.setCreatedAt(fixed);
            inv.prePersist();

            assertThat(inv.getCreatedAt()).isEqualTo(fixed);
        }

        @Test
        void preUpdate_updatesUpdatedAt() {
            CollaborationInvitation inv = new CollaborationInvitation();
            inv.prePersist();
            Instant before = inv.getUpdatedAt();
            inv.preUpdate();

            assertThat(inv.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class InfluencerRatingCallbacks {
        @Test
        void prePersist_setsCreatedAtWhenNull() {
            InfluencerRating r = new InfluencerRating();
            r.prePersist();

            assertThat(r.getCreatedAt()).isNotNull();
            assertThat(r.getCreatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }

        @Test
        void prePersist_doesNotOverwriteCreatedAtWhenSet() {
            InfluencerRating r = new InfluencerRating();
            Instant fixed = Instant.parse("2018-08-08T08:08:08Z");
            r.setCreatedAt(fixed);
            r.prePersist();

            assertThat(r.getCreatedAt()).isEqualTo(fixed);
        }
    }

}
