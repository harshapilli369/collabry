package com.group4.backend.repository;

import com.group4.backend.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByUserIdOrderByCreatedAtDesc(Long userId);
}
