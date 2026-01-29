package com.empowerher.controllers;

import com.empowerher.entities.Bookmark;
import com.empowerher.entities.Scheme;
import com.empowerher.entities.User;
import com.empowerher.repositories.BookmarkRepository;
import com.empowerher.repositories.SchemeRepository;
import com.empowerher.repositories.UserRepository;
import com.empowerher.services.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private SchemeService schemeService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("Getting current user: " + username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            System.out.println("=== USER DASHBOARD ACCESSED ===");
            User user = getCurrentUser();
            System.out.println("User: " + user.getUsername() + " | Role: " + user.getRole());
            
            List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
            List<Scheme> allSchemes = schemeService.getActiveSchemes();
            
            // Get bookmarked scheme IDs
            List<Long> bookmarkedIds = bookmarks.stream()
                    .map(bookmark -> bookmark.getScheme().getId())
                    .collect(Collectors.toList());
            
            // Get recent schemes (last 6)
            List<Scheme> recentSchemes = allSchemes.stream()
                    .limit(6)
                    .collect(Collectors.toList());
            
            System.out.println("Bookmarks: " + bookmarks.size());
            System.out.println("All Schemes: " + allSchemes.size());
            
            model.addAttribute("user", user);
            model.addAttribute("bookmarks", bookmarks);
            model.addAttribute("schemes", recentSchemes);
            model.addAttribute("bookmarkedIds", bookmarkedIds);
            model.addAttribute("bookmarksCount", bookmarks.size());
            model.addAttribute("schemesCount", allSchemes.size());
            
            return "user/dashboard";
        } catch (Exception e) {
            System.out.println("Error in user dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "user/dashboard";
        }
    }

    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        User user = getCurrentUser();
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("bookmarksCount", bookmarks.size());
        
        return "user/bookmarks";
    }

    @PostMapping("/bookmark/{schemeId}")
    @ResponseBody
    public Map<String, Object> toggleBookmark(@PathVariable Long schemeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser();
            Scheme scheme = schemeRepository.findById(schemeId)
                    .orElseThrow(() -> new RuntimeException("Scheme not found"));
            
            // Check if already bookmarked
            Bookmark existingBookmark = bookmarkRepository.findByUserAndScheme(user, scheme)
                    .orElse(null);
            
            if (existingBookmark != null) {
                // Remove bookmark
                bookmarkRepository.delete(existingBookmark);
                response.put("bookmarked", false);
                response.put("message", "Bookmark removed");
            } else {
                // Add bookmark
                Bookmark bookmark = new Bookmark();
                bookmark.setUser(user);
                bookmark.setScheme(scheme);
                bookmarkRepository.save(bookmark);
                response.put("bookmarked", true);
                response.put("message", "Bookmark added");
            }
            
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username, 
                               @RequestParam String email,
                               Model model) {
        try {
            User user = getCurrentUser();
            
            // Check if username already exists (excluding current user)
            if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                model.addAttribute("error", "Username already exists");
                model.addAttribute("user", user);
                return "user/profile";
            }
            
            // Check if email already exists (excluding current user)
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                model.addAttribute("error", "Email already registered");
                model.addAttribute("user", user);
                return "user/profile";
            }
            
            user.setUsername(username);
            user.setEmail(email);
            userRepository.save(user);
            
            model.addAttribute("success", "Profile updated successfully!");
            model.addAttribute("user", user);
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating profile: " + e.getMessage());
            model.addAttribute("user", getCurrentUser());
            return "user/profile";
        }
    }

    @GetMapping("/schemes")
    public String userSchemes(Model model) {
        User user = getCurrentUser();
        List<Scheme> schemes = schemeService.getActiveSchemes();
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        List<Long> bookmarkedIds = bookmarks.stream()
                .map(bookmark -> bookmark.getScheme().getId())
                .collect(Collectors.toList());
        
        model.addAttribute("user", user);
        model.addAttribute("schemes", schemes);
        model.addAttribute("bookmarkedIds", bookmarkedIds);
        model.addAttribute("schemesCount", schemes.size());
        
        return "user/schemes";
    }
}