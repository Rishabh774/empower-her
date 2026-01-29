package com.empowerher.services;

import com.empowerher.entities.Comment;
import com.empowerher.entities.Scheme;
import com.empowerher.entities.User;
import com.empowerher.repositories.CommentRepository;
import com.empowerher.repositories.SchemeRepository;
import com.empowerher.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    public List<Comment> getCommentsForScheme(Long schemeId) {
        return commentRepository.findBySchemeIdWithUser(schemeId);
    }

    public Comment addComment(Long schemeId, Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Scheme not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setScheme(scheme);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setActive(true);

        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if user owns the comment
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own comments");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if user owns the comment or is admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isOwner = comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You can only delete your own comments");
        }

        // Soft delete by setting active to false
        comment.setActive(false);
        commentRepository.save(comment);
    }

    public long getCommentCountForScheme(Long schemeId) {
        return commentRepository.countBySchemeIdAndActiveTrue(schemeId);
    }

    public List<Comment> getRecentComments(int limit) {
        return commentRepository.findRecentComments(limit);
    }

    public List<Comment> getUserComments(Long userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}