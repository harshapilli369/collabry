package com.group4.backend.repository;

import com.group4.backend.model.InfluencerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfluencerProfileRepository extends JpaRepository<InfluencerProfile, Long> {
    Optional<InfluencerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
