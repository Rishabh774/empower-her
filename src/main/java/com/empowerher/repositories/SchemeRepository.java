// SchemeRepository.java mein searchSchemes method ko update karen:
package com.empowerher.repositories;

import com.empowerher.entities.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    
    // Find active schemes with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE s.active = true")
    List<Scheme> findByActiveTrue();
    
    // Find all schemes with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category")
    List<Scheme> findAllWithCategory();
    
    // Find scheme by ID with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE s.id = :id")
    Optional<Scheme> findByIdWithCategory(@Param("id") Long id);
    
    // Find schemes by level with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE s.level = :level")
    List<Scheme> findByLevel(@Param("level") String level);
    
    // Find schemes by category ID
    @Query("SELECT s FROM Scheme s WHERE s.category.id = :categoryId AND s.active = true")
    List<Scheme> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Search schemes by title or description with category eagerly fetched - FIXED
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE " +
           "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.shortDesc) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.fullDesc) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "s.active = true")
    List<Scheme> searchSchemes(@Param("query") String query);
    
    // Find top shared schemes with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE s.active = true ORDER BY s.shareCount DESC")
    List<Scheme> findTopSharedSchemes();
    
    // Count schemes by category
    @Query("SELECT COUNT(s) FROM Scheme s WHERE s.category.id = :categoryId AND s.active = true")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    // Find schemes with pagination with category eagerly fetched
    @Query("SELECT s FROM Scheme s JOIN FETCH s.category WHERE s.active = true ORDER BY s.createdAt DESC")
    List<Scheme> findRecentSchemes();
}