package com.campusswap.backend.repository;

import com.campusswap.backend.model.College;
import com.campusswap.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCollegeAndIsSoldFalse(College college);

    List<Product> findByCategoryAndCollegeAndIsSoldFalse(String category, College college);

    List<Product> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

    @Query("SELECT p FROM Product p WHERE p.college = ?1 AND p.isSold = false ORDER BY p.createdAt DESC")
    List<Product> findRecentProductsByCollege(College college);

    // SWIPE FEED — paginated, college scoped, exclude liked + own products
    @Query("""
        SELECT p FROM Product p
        WHERE p.college = :college
        AND p.isSold = false
        AND p.seller.id != :userId
        AND p.id NOT IN (
            SELECT l.product.id FROM Like l WHERE l.user.id = :userId
        )
        ORDER BY p.createdAt DESC
    """)
    Page<Product> findFeedProducts(
            @Param("college") College college,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // SWIPE FEED RESET — show skipped products (not liked, not own, not sold)
    // Prioritize: new first, then by price
    @Query("""
        SELECT p FROM Product p
        WHERE p.college = :college
        AND p.isSold = false
        AND p.seller.id != :userId
        ORDER BY p.createdAt DESC, p.sellingPrice ASC
    """)
    Page<Product> findFeedProductsReset(
            @Param("college") College college,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.college = :college
        AND p.isSold = false
        AND ((:keyword = '') OR
             LOWER(CAST(p.title AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND ((:category = '') OR p.category = :category)
        AND ((:condition = '') OR p.condition = :condition)
        AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
        AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
        ORDER BY p.createdAt DESC
    """)
    List<Product> searchProducts(
            @Param("college") College college,
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("condition") String condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
