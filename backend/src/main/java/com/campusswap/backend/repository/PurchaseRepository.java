package com.campusswap.backend.repository;

import com.campusswap.backend.model.Note;
import com.campusswap.backend.model.Purchase;
import com.campusswap.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    boolean existsByBuyerAndNote(User buyer, Note note);
    Optional<Purchase> findByBuyerAndNote(User buyer, Note note);
}
