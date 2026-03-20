package com.group4.backend.repository;

import com.group4.backend.model.InfluencerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InfluencerProfileRepository extends JpaRepository<InfluencerProfile, Long>, JpaSpecificationExecutor<InfluencerProfile> {
    Optional<InfluencerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
