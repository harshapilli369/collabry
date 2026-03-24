package com.group4.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Same secret encoding as {@link JwtUtils} (dev key) — required to build expired tokens for tests.
 */
class JwtUtilsTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtUtils jwtUtils;

    private static Key signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    void generateToken_withoutRememberMe_expiresInAboutOneDay() {
        String token = jwtUtils.generateToken("user@test.com", "BRAND", false);

        Date exp = jwtUtils.extractClaim(token, Claims::getExpiration);
        long msLeft = exp.getTime() - System.currentTimeMillis();

        assertThat(msLeft).isBetween(TimeUnit.HOURS.toMillis(23), TimeUnit.HOURS.toMillis(25));
    }

    @Test
    void generateToken_withRememberMe_expiresInAboutSevenDays() {
        String token = jwtUtils.generateToken("user@test.com", "INFLUENCER", true);

        Date exp = jwtUtils.extractClaim(token, Claims::getExpiration);
        long msLeft = exp.getTime() - System.currentTimeMillis();

        assertThat(msLeft).isBetween(TimeUnit.DAYS.toMillis(6) + TimeUnit.HOURS.toMillis(20),
                TimeUnit.DAYS.toMillis(7) + TimeUnit.HOURS.toMillis(4));
    }

    @Test
    void extractUsername_roundTrip() {
        String token = jwtUtils.generateToken("roundtrip@test.com", "ADMIN", false);

        assertThat(jwtUtils.extractUsername(token)).isEqualTo("roundtrip@test.com");
    }

    @Test
    void extractClaim_returnsRoleClaim() {
        String token = jwtUtils.generateToken("a@test.com", "BRAND", false);

        String role = jwtUtils.extractClaim(token, c -> c.get("role", String.class));

        assertThat(role).isEqualTo("BRAND");
    }

    @Test
    void isTokenValid_returnsTrueWhenEmailMatchesAndTokenNotExpired() {
        String token = jwtUtils.generateToken("valid@test.com", "USER", false);

        assertThat(jwtUtils.isTokenValid(token, "valid@test.com")).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseWhenEmailDoesNotMatch() {
        String token = jwtUtils.generateToken("a@test.com", "USER", false);

        assertThat(jwtUtils.isTokenValid(token, "b@test.com")).isFalse();
    }

    @Test
    void isTokenValid_whenJwtExpired_throwsExpiredJwtException() {
        String expiredToken = Jwts.builder()
                .setSubject("expired@test.com")
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();

        // JJWT rejects expired tokens during parse (before private isTokenExpired runs).
        assertThatThrownBy(() -> jwtUtils.isTokenValid(expiredToken, "expired@test.com"))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractUsername_throwsOnMalformedToken() {
        assertThatThrownBy(() -> jwtUtils.extractUsername("not-a-jwt"))
                .isInstanceOf(Exception.class);
    }
}
