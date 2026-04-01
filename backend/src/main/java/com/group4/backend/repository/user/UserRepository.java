package com.group4.backend.repository.user;

import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    Page<User> findByRoleInOrderByCreatedAtDesc(Collection<Role> roles, Pageable pageable);

    Page<User> findByRoleNotOrderByCreatedAtDesc(Role role, Pageable pageable);
}
