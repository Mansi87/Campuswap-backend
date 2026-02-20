package com.campusswap.backend.repository;

import com.campusswap.backend.model.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollegeRepository extends JpaRepository<College, UUID> {
    Optional<College> findByEmailDomain(String emailDomain);

    Optional<College> findByName(String name);
}
