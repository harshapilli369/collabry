package com.group4.backend.dto;

import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.auth.ResetPasswordRequest;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.dto.auth.SignupResponse;
import com.group4.backend.dto.auth.TokenRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.dto.invitation.InvitationCampaignView;
import com.group4.backend.dto.invitation.InvitationDetailResponse;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.dto.invitation.NegotiationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.invitation.UpdateInvitationRequest;
import com.group4.backend.dto.payment.PaymentRequest;
import com.group4.backend.dto.payment.PaymentResponse;
import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.dto.profile.BrandProfileResponse;
import com.group4.backend.dto.profile.InfluencerProfileRequest;
import com.group4.backend.dto.profile.InfluencerProfileResponse;
import com.group4.backend.dto.profile.InfluencerRecommendationDTO;
import com.group4.backend.dto.profile.InfluencerSearchResult;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.dto.rating.RatingRequest;
import com.group4.backend.dto.rating.RatingResponse;
import com.group4.backend.model.Role;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Covers branches and accessors under {@code com.group4.backend.dto} for JaCoCo
 * (notably {@link SignupRequest#isAllowedRole(Role)} and all POJO accessors).
 */
class DtoBranchCoverageTest {

    @Nested
    class SignupRequestAllowedRole {
        @Test
        void returnsFalseWhenRoleIsNull() {
            assertThat(SignupRequest.isAllowedRole(null)).isFalse();
        }

        @Test
        void returnsFalseWhenRoleIsAdmin() {
            assertThat(SignupRequest.isAllowedRole(Role.ADMIN)).isFalse();
        }

        @Test
        void returnsFalseWhenRoleIsUser() {
            assertThat(SignupRequest.isAllowedRole(Role.USER)).isFalse();
        }

        @Test
        void returnsTrueWhenRoleIsBrand() {
            assertThat(SignupRequest.isAllowedRole(Role.BRAND)).isTrue();
        }

        @Test
        void returnsTrueWhenRoleIsInfluencer() {
            assertThat(SignupRequest.isAllowedRole(Role.INFLUENCER)).isTrue();
        }
    }

    @Nested
    class SignupRequestAccessors {
        @Test
        void noArgConstructorAndSettersRoundTrip() {
            SignupRequest r = new SignupRequest();
            r.setEmail("a@b.com");
            r.setPassword("secret12");
            r.setRole(Role.BRAND);
            assertThat(r.getEmail()).isEqualTo("a@b.com");
            assertThat(r.getPassword()).isEqualTo("secret12");
            assertThat(r.getRole()).isEqualTo(Role.BRAND);
        }

        @Test
        void allArgsConstructorPopulatesFields() {
            SignupRequest r = new SignupRequest("x@y.com", "pw123456", Role.INFLUENCER);
            assertThat(r.getEmail()).isEqualTo("x@y.com");
            assertThat(r.getPassword()).isEqualTo("pw123456");
            assertThat(r.getRole()).isEqualTo(Role.INFLUENCER);
        }
    }

    @Nested
    class MultiArgConstructors {
        @Test
        void authResponse() {
            AuthResponse r = new AuthResponse("t", "e@e.com", Role.ADMIN, 5L, true);
            assertThat(r.getToken()).isEqualTo("t");
            assertThat(r.getEmail()).isEqualTo("e@e.com");
            assertThat(r.getRole()).isEqualTo(Role.ADMIN);
            assertThat(r.getId()).isEqualTo(5L);
            assertThat(r.isVerified()).isTrue();
        }

        @Test
        void loginRequest() {
            LoginRequest r = new LoginRequest("a@b.com", "pw");
            assertThat(r.getEmail()).isEqualTo("a@b.com");
            assertThat(r.getPassword()).isEqualTo("pw");
        }

        @Test
        void tokenRequest() {
            TokenRequest r = new TokenRequest("tok");
            assertThat(r.getToken()).isEqualTo("tok");
        }

        @Test
        void signupResponse() {
            SignupResponse r = new SignupResponse("hello");
            assertThat(r.getMessage()).isEqualTo("hello");
        }

        @Test
        void influencerSearchResult() {
            InfluencerSearchResult r = new InfluencerSearchResult(1L, "e@e.com", "Name");
            assertThat(r.getId()).isEqualTo(1L);
            assertThat(r.getEmail()).isEqualTo("e@e.com");
            assertThat(r.getDisplayName()).isEqualTo("Name");
        }
    }

    @Nested
    class BooleanFlagAccessors {
        @Test
        void loginRequestRememberMe() {
            LoginRequest r = new LoginRequest();
            r.setRememberMe(true);
            assertThat(r.isRememberMe()).isTrue();
            r.setRememberMe(false);
            assertThat(r.isRememberMe()).isFalse();
        }

        @Test
        void influencerProfileRequestSaveAsDraft() {
            InfluencerProfileRequest r = new InfluencerProfileRequest();
            assertThat(r.isSaveAsDraft()).isFalse();
            r.setSaveAsDraft(true);
            assertThat(r.isSaveAsDraft()).isTrue();
        }

        @Test
        void influencerProfileResponseComplete() {
            InfluencerProfileResponse r = new InfluencerProfileResponse();
            r.setComplete(true);
            assertThat(r.isComplete()).isTrue();
            r.setComplete(false);
            assertThat(r.isComplete()).isFalse();
        }

        @Test
        void authResponseVerified() {
            AuthResponse r = new AuthResponse();
            r.setVerified(true);
            assertThat(r.isVerified()).isTrue();
            r.setVerified(false);
            assertThat(r.isVerified()).isFalse();
        }
    }

    /**
     * Every DTO with a no-arg constructor: set each JavaBean property via reflection and read it back.
     */
    @Nested
    class ReflectiveBeanRoundTrips {
        private static final Class<?>[] DTO_CLASSES = {
                AuthResponse.class,
                BrandProfileRequest.class,
                BrandProfileResponse.class,
                CampaignRequest.class,
                CampaignResponse.class,
                InfluencerRecommendationDTO.class,
                InvitationCampaignView.class,
                InfluencerProfileRequest.class,
                InfluencerProfileResponse.class,
                InfluencerSearchResult.class,
                InvitationDetailResponse.class,
                InvitationRequest.class,
                InvitationResponse.class,
                LoginRequest.class,
                NegotiationRequest.class,
                PaymentRequest.class,
                PaymentResponse.class,
                RatingRequest.class,
                RatingResponse.class,
                ResetPasswordRequest.class,
                RespondRequest.class,
                SignupRequest.class,
                SignupResponse.class,
                SocialLinkRequest.class,
                TokenRequest.class,
                UpdateInvitationRequest.class,
        };

        @TestFactory
        Stream<DynamicTest> eachDto_roundTripsAllBeanProperties() {
            return Arrays.stream(DTO_CLASSES).map(clazz -> dynamicTest(clazz.getSimpleName(), () -> roundTripBeanProperties(clazz)));
        }

        private static void roundTripBeanProperties(Class<?> beanClass) throws Exception {
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if ("class".equals(pd.getName())) {
                    continue;
                }
                Method write = pd.getWriteMethod();
                Method read = pd.getReadMethod();
                if (write == null || read == null) {
                    continue;
                }
                Class<?> propType = pd.getPropertyType();
                Object value = sampleValue(propType);
                write.invoke(bean, value);
                Object actual = read.invoke(bean);
                assertThat(actual).usingRecursiveComparison().isEqualTo(value);
            }
        }

        private static Object sampleValue(Class<?> type) {
            if (type == String.class) {
                return "sample";
            }
            if (type == Long.class || type == long.class) {
                return type == long.class ? 11L : Long.valueOf(11L);
            }
            if (type == Integer.class || type == int.class) {
                return type == int.class ? 7 : Integer.valueOf(7);
            }
            if (type == Double.class) {
                return Double.valueOf(3.25);
            }
            if (type == double.class) {
                return 3.25;
            }
            if (type == Boolean.class) {
                return Boolean.TRUE;
            }
            if (type == boolean.class) {
                return true;
            }
            if (type == BigDecimal.class) {
                return new BigDecimal("12.50");
            }
            if (type == LocalDate.class) {
                return LocalDate.of(2024, 3, 15);
            }
            if (type == Instant.class) {
                return Instant.parse("2024-03-15T12:00:00Z");
            }
            if (List.class.isAssignableFrom(type)) {
                return List.of();
            }
            if (type.isEnum()) {
                return type.getEnumConstants()[0];
            }
            if (type == CampaignResponse.class) {
                CampaignResponse c = new CampaignResponse();
                c.setId(42L);
                c.setName("n");
                return c;
            }
            if (type == InvitationCampaignView.class) {
                InvitationCampaignView v = new InvitationCampaignView();
                v.setId(42L);
                v.setName("n");
                return v;
            }
            throw new IllegalArgumentException("Add sample for property type: " + type.getName());
        }
    }
}
