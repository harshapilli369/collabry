package com.group4.backend.repository.user;

import com.group4.backend.model.VerificationRequest;
import com.group4.backend.model.VerificationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    
    List<VerificationRequest> findByStatus(VerificationRequestStatus status);
    
    Optional<VerificationRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<VerificationRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
