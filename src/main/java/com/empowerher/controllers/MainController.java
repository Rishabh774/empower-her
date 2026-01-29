package com.empowerher.controllers;

import com.empowerher.entities.Scheme;
import com.empowerher.entities.Comment;
import com.empowerher.entities.User;
import com.empowerher.events.UserRegisteredEvent;
import com.empowerher.repositories.BookmarkRepository;
import com.empowerher.repositories.CategoryRepository;
import com.empowerher.repositories.SchemeRepository;
import com.empowerher.repositories.UserRepository;
import com.empowerher.services.CommentService;
import com.empowerher.services.SchemeService;
import com.empowerher.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private CommentService commentService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private SchemeService schemeService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Home page redirect
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/home";
    }

    // Home page - FIXED: Properly connected to database
    @GetMapping("/home")
    public String home(Model model) {
        try {
            System.out.println("=== LOADING HOME PAGE ===");
            
            List<Scheme> schemes = schemeRepository.findByActiveTrue();
            System.out.println("Found " + schemes.size() + " active schemes");
            
            // Limit to 6 schemes for home page
            List<Scheme> featuredSchemes = schemes.stream()
                    .limit(6)
                    .collect(Collectors.toList());
            
            // Get scheme counts by level for statistics
            long centralCount = schemes.stream().filter(s -> "CENTRAL".equals(s.getLevel())).count();
            long stateCount = schemes.stream().filter(s -> "STATE".equals(s.getLevel())).count();
            long districtCount = schemes.stream().filter(s -> "DISTRICT".equals(s.getLevel())).count();
            long privateCount = schemes.stream().filter(s -> "PRIVATE".equals(s.getLevel())).count();
            
            // Debug output
            System.out.println("Central schemes: " + centralCount);
            System.out.println("State schemes: " + stateCount);
            System.out.println("District schemes: " + districtCount);
            System.out.println("Private schemes: " + privateCount);
            System.out.println("Featured schemes to display: " + featuredSchemes.size());
            
            model.addAttribute("schemes", featuredSchemes);
            model.addAttribute("totalSchemes", schemes.size());
            model.addAttribute("centralCount", centralCount);
            model.addAttribute("stateCount", stateCount);
            model.addAttribute("districtCount", districtCount);
            model.addAttribute("privateCount", privateCount);
            
            return "home";
        } catch (Exception e) {
            System.err.println("ERROR in home page: " + e.getMessage());
            e.printStackTrace();
            // Return safe default values
            model.addAttribute("schemes", new ArrayList<Scheme>());
            model.addAttribute("totalSchemes", 0);
            model.addAttribute("centralCount", 0);
            model.addAttribute("stateCount", 0);
            model.addAttribute("districtCount", 0);
            model.addAttribute("privateCount", 0);
            return "home";
        }
    }

    // Categories page - FIXED: Added proper error handling and counts
    @GetMapping("/categories")
    public String categories(@RequestParam(required = false) String level, Model model) {
        try {
            List<Scheme> schemes;
            if (level != null && !level.isEmpty()) {
                schemes = schemeRepository.findByLevel(level);
            } else {
                schemes = schemeRepository.findByActiveTrue();
            }
            
            // Calculate counts for filter buttons
            List<Scheme> allSchemes = schemeRepository.findByActiveTrue();
            long centralCount = allSchemes.stream().filter(s -> "CENTRAL".equals(s.getLevel())).count();
            long stateCount = allSchemes.stream().filter(s -> "STATE".equals(s.getLevel())).count();
            long districtCount = allSchemes.stream().filter(s -> "DISTRICT".equals(s.getLevel())).count();
            long privateCount = allSchemes.stream().filter(s -> "PRIVATE".equals(s.getLevel())).count();
            
            model.addAttribute("schemes", schemes);
            model.addAttribute("selectedLevel", level);
            model.addAttribute("centralCount", centralCount);
            model.addAttribute("stateCount", stateCount);
            model.addAttribute("districtCount", districtCount);
            model.addAttribute("privateCount", privateCount);
            
            return "categories";
        } catch (Exception e) {
            System.err.println("ERROR in categories page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading categories: " + e.getMessage());
            model.addAttribute("schemes", new ArrayList<Scheme>());
            model.addAttribute("centralCount", 0);
            model.addAttribute("stateCount", 0);
            model.addAttribute("districtCount", 0);
            model.addAttribute("privateCount", 0);
            return "categories";
        }
    }

    // Login page
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "registered", required = false) String registered,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please login with your credentials.");
        }
        
        return "login";
    }

    // Registration page
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Process registration - UPDATED with Event Publisher
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                              BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists. Please choose a different username.");
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already registered. Please use a different email address.");
            return "register";
        }

        try {
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            User savedUser = userService.createUser(user);
            
            // Event publish karen for email sending
            eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser));
            System.out.println("User registration event published for: " + savedUser.getEmail());
            
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        try {
            System.out.println("=== SEARCH REQUEST ===");
            System.out.println("Search query: " + query);
            
            if (query == null || query.trim().isEmpty()) {
                model.addAttribute("error", "Please enter a search term");
                model.addAttribute("schemes", new ArrayList<Scheme>());
                model.addAttribute("searchQuery", "");
                return "search-results";
            }
            
            // Trim and validate query
            String searchQuery = query.trim();
            if (searchQuery.length() < 2) {
                model.addAttribute("error", "Please enter at least 2 characters to search");
                model.addAttribute("schemes", new ArrayList<Scheme>());
                model.addAttribute("searchQuery", searchQuery);
                return "search-results";
            }
            
            List<Scheme> schemes = schemeService.searchSchemes(searchQuery);
            System.out.println("Search returned " + schemes.size() + " results");
            
            model.addAttribute("schemes", schemes);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("resultsCount", schemes.size());
            
            return "search-results";
        } catch (Exception e) {
            System.err.println("ERROR in search: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Search failed: " + e.getMessage());
            model.addAttribute("schemes", new ArrayList<Scheme>());
            model.addAttribute("searchQuery", query);
            model.addAttribute("resultsCount", 0);
            return "search-results";
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        try {
            List<Scheme> allSchemes = schemeRepository.findByActiveTrue();
            model.addAttribute("totalSchemes", allSchemes.size());
        } catch (Exception e) {
            model.addAttribute("totalSchemes", 0);
        }
        model.addAttribute("pageTitle", "About EmpowerHer");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us");
        return "contact";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("error", "You don't have permission to access this page.");
        return "access-denied";
    }
    
    // Scheme Detail Page
    @GetMapping("/scheme/{id}")
    public String schemeDetail(@PathVariable Long id, Model model, 
                              HttpServletRequest request) {
        try {
            System.out.println("Loading scheme details for ID: " + id);
            
            Scheme scheme = schemeService.getSchemeById(id);
            if (scheme == null || !scheme.isActive()) {
                return "redirect:/categories?error=Scheme not found";
            }
            
            // Get current user for bookmark status
            User currentUser = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && 
                    !(authentication.getPrincipal() instanceof String)) {
                    String username = authentication.getName();
                    currentUser = userRepository.findByUsername(username).orElse(null);
                }
            } catch (Exception e) {
                System.out.println("No authenticated user found");
            }
            
            // Check if current user has bookmarked this scheme
            boolean isBookmarked = false;
            if (currentUser != null) {
                isBookmarked = bookmarkRepository.existsByUserAndScheme(currentUser, scheme);
            }
            
            // Get comments for this scheme
            List<Comment> comments = commentService.getCommentsForScheme(id);
            
            // Get related schemes (same category)
            List<Scheme> relatedSchemes = schemeRepository.findByCategoryId(scheme.getCategory().getId())
                    .stream()
                    .filter(s -> !s.getId().equals(id) && s.isActive())
                    .limit(4)
                    .collect(Collectors.toList());
            
            model.addAttribute("scheme", scheme);
            model.addAttribute("isBookmarked", isBookmarked);
            model.addAttribute("comments", comments);
            model.addAttribute("commentCount", comments.size());
            model.addAttribute("relatedSchemes", relatedSchemes);
            model.addAttribute("currentUser", currentUser);
            
            return "scheme-detail";
        } catch (Exception e) {
            System.err.println("ERROR loading scheme details: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/categories?error=Error loading scheme details";
        }
    }
}