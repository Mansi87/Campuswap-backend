package com.campusswap.backend.repository;

import com.campusswap.backend.model.Like;
import com.campusswap.backend.model.Product;
import com.campusswap.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    // Find all products liked by a user
    List<Like> findByUserOrderByCreatedAtDesc(User user);

    // Check if user already liked a product
    Optional<Like> findByUserAndProduct(User user, Product product);

    // Check if like exists
    boolean existsByUserAndProduct(User user, Product product);

    // Delete a like
    void deleteByUserAndProduct(User user, Product product);

    // Count likes for a product
    long countByProduct(Product product);
}
