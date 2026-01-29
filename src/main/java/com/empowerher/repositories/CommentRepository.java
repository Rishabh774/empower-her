package com.empowerher.repositories;

import com.empowerher.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find all active comments for a scheme, ordered by creation date (newest first)
    List<Comment> findBySchemeIdAndActiveTrueOrderByCreatedAtDesc(Long schemeId);
    
    // Find comments by user
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Count comments for a scheme
    long countBySchemeIdAndActiveTrue(Long schemeId);
    
    // Find recent comments with pagination
    @Query("SELECT c FROM Comment c WHERE c.active = true ORDER BY c.createdAt DESC LIMIT :limit")
    List<Comment> findRecentComments(@Param("limit") int limit);
    
    // Find comments by scheme with user data eagerly fetched
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.scheme.id = :schemeId AND c.active = true ORDER BY c.createdAt DESC")
    List<Comment> findBySchemeIdWithUser(@Param("schemeId") Long schemeId);
}