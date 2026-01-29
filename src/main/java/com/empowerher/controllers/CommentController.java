package com.empowerher.controllers;

import com.empowerher.entities.Comment;
import com.empowerher.entities.User;
import com.empowerher.repositories.UserRepository;
import com.empowerher.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @GetMapping("/scheme/{schemeId}")
    public ResponseEntity<Map<String, Object>> getSchemeComments(@PathVariable Long schemeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Comment> comments = commentService.getCommentsForScheme(schemeId);
            long commentCount = commentService.getCommentCountForScheme(schemeId);
            
            response.put("success", true);
            response.put("comments", comments);
            response.put("commentCount", commentCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/scheme/{schemeId}")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long schemeId, 
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            User currentUser = getCurrentUser();
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Comment content cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Comment comment = commentService.addComment(schemeId, currentUser.getId(), content);
            
            response.put("success", true);
            response.put("comment", comment);
            response.put("message", "Comment added successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            User currentUser = getCurrentUser();
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Comment content cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Comment updatedComment = commentService.updateComment(commentId, currentUser.getId(), content);
            
            response.put("success", true);
            response.put("comment", updatedComment);
            response.put("message", "Comment updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            commentService.deleteComment(commentId, currentUser.getId());
            
            response.put("success", true);
            response.put("message", "Comment deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentComments(
            @RequestParam(defaultValue = "5") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Comment> recentComments = commentService.getRecentComments(limit);
            
            response.put("success", true);
            response.put("comments", recentComments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}