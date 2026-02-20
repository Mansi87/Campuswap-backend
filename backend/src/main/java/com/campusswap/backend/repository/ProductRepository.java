package com.campusswap.backend.repository;

import com.campusswap.backend.model.College;
import com.campusswap.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCollegeAndIsSoldFalse(College college);

    List<Product> findByCategoryAndCollegeAndIsSoldFalse(String category, College college);

    List<Product> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

    @Query("SELECT p FROM Product p WHERE p.college = ?1 AND p.isSold = false ORDER BY p.createdAt DESC")
    List<Product> findRecentProductsByCollege(College college);
}
