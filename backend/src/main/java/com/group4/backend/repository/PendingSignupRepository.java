package com.group4.backend.repository;

import com.group4.backend.model.PendingSignup;

import java.util.Optional;

public interface PendingSignupRepository extends org.springframework.data.jpa.repository.JpaRepository<PendingSignup, Long> {

    Optional<PendingSignup> findByToken(String token);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);
}
