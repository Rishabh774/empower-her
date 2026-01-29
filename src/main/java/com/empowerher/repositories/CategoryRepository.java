package com.empowerher.repositories;

import com.empowerher.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find categories by level
    List<Category> findByLevel(String level);
    
    // Find category by name (case insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Find active categories with schemes
    @Query("SELECT DISTINCT c FROM Category c JOIN c.schemes s WHERE s.active = true")
    List<Category> findCategoriesWithActiveSchemes();
    
    // Check if category exists by name
    boolean existsByName(String name);
    
    // Find category by name exactly
    Optional<Category> findByName(String name);
}