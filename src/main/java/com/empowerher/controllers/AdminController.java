package com.empowerher.controllers;

import com.empowerher.dto.SchemeDto;
import com.empowerher.entities.Category;
import com.empowerher.entities.Scheme;
import com.empowerher.entities.User;
import com.empowerher.repositories.CategoryRepository;
import com.empowerher.repositories.SchemeRepository;
import com.empowerher.repositories.UserRepository;
import com.empowerher.services.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SchemeService schemeService;
    
    @Autowired
    private UserRepository userRepository;
    

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

 // AdminController.java में dashboard method update करें
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<Scheme> schemes = schemeService.getAllSchemes();
            long totalSchemes = schemes.size();
            long activeSchemes = schemes.stream().filter(Scheme::isActive).count();
            
            // Limit schemes for recent schemes table (show only 5)
            List<Scheme> recentSchemes = schemes.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            
            model.addAttribute("totalSchemes", totalSchemes);
            model.addAttribute("activeSchemes", activeSchemes);
            model.addAttribute("schemes", recentSchemes); // Pass only recent schemes
            
            return "admin/dashboard";
        } catch (Exception e) {
            System.err.println("ERROR in admin dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            model.addAttribute("totalSchemes", 0);
            model.addAttribute("activeSchemes", 0);
            model.addAttribute("schemes", new ArrayList<Scheme>());
            return "admin/dashboard";
        }
    }
    
    
    @GetMapping("/schemes")
    public String schemes(Model model) {
        try {
            List<Scheme> schemes = schemeService.getAllSchemes();
            List<Category> categories = categoryRepository.findAll();
            
            model.addAttribute("schemes", schemes);
            model.addAttribute("categories", categories);
            model.addAttribute("schemeDto", new SchemeDto());
            
            return "admin/schemes";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading schemes: " + e.getMessage());
            return "admin/schemes";
        }
    }

    @GetMapping("/schemes/add")
    public String showAddSchemeForm(Model model) {
        try {
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("schemeDto", new SchemeDto());
            model.addAttribute("categories", categories);
            return "admin/add-scheme";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading form: " + e.getMessage());
            return "admin/add-scheme";
        }
    }

 // AdminController.java mein sirf addScheme method update karen:
    @PostMapping("/schemes/add")
    public String addScheme(@ModelAttribute SchemeDto schemeDto, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ADDING NEW SCHEME ===");
            System.out.println("Title: " + schemeDto.getTitle());
            System.out.println("Category ID: " + schemeDto.getCategoryId());
            System.out.println("Level: " + schemeDto.getLevel());
            System.out.println("YouTube URL: " + schemeDto.getVideoUrl());
            
            // Debug file information
            if (schemeDto.getImageFile() != null && !schemeDto.getImageFile().isEmpty()) {
                System.out.println("Image File: " + schemeDto.getImageFile().getOriginalFilename());
                System.out.println("Image Size: " + schemeDto.getImageFile().getSize() + " bytes");
            } else {
                System.out.println("No image file provided");
            }
            
            if (schemeDto.getPdfFile() != null && !schemeDto.getPdfFile().isEmpty()) {
                System.out.println("PDF File: " + schemeDto.getPdfFile().getOriginalFilename());
                System.out.println("PDF Size: " + schemeDto.getPdfFile().getSize() + " bytes");
            } else {
                System.out.println("No PDF file provided");
            }
            
            // Validate required fields
            if (schemeDto.getTitle() == null || schemeDto.getTitle().trim().isEmpty()) {
                model.addAttribute("error", "Scheme title is required");
                List<Category> categories = categoryRepository.findAll();
                model.addAttribute("categories", categories);
                return "admin/add-scheme";
            }
            
            if (schemeDto.getCategoryId() == null) {
                model.addAttribute("error", "Category is required");
                List<Category> categories = categoryRepository.findAll();
                model.addAttribute("categories", categories);
                return "admin/add-scheme";
            }
            
            Scheme createdScheme = schemeService.createScheme(schemeDto);
            System.out.println("Scheme created successfully with ID: " + createdScheme.getId());
            
            redirectAttributes.addFlashAttribute("success", "Scheme added successfully!");
            return "redirect:/admin/schemes?success=true";
        } catch (IOException e) {
            System.err.println("ERROR uploading files: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error uploading files: " + e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "admin/add-scheme";
        } catch (Exception e) {
            System.err.println("ERROR adding scheme: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error adding scheme: " + e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "admin/add-scheme";
        }
    }

    @GetMapping("/schemes/edit/{id}")
    public String showEditSchemeForm(@PathVariable Long id, Model model) {
        try {
            Scheme scheme = schemeService.getSchemeById(id);
            if (scheme == null) {
                return "redirect:/admin/schemes?error=Scheme not found";
            }
            
            List<Category> categories = categoryRepository.findAll();
            
            SchemeDto schemeDto = new SchemeDto();
            schemeDto.setId(scheme.getId());
            schemeDto.setTitle(scheme.getTitle());
            schemeDto.setCategoryId(scheme.getCategory().getId());
            schemeDto.setLevel(scheme.getLevel());
            schemeDto.setShortDesc(scheme.getShortDesc());
            schemeDto.setFullDesc(scheme.getFullDesc());
            schemeDto.setApplyLink(scheme.getApplyLink());
            schemeDto.setVideoUrl(scheme.getVideoUrl());
            schemeDto.setBlogContent(scheme.getBlogContent());
            schemeDto.setImageUrl(scheme.getImageUrl());
            schemeDto.setPdfUrl(scheme.getPdfUrl());
            
            model.addAttribute("schemeDto", schemeDto);
            model.addAttribute("categories", categories);
            return "admin/edit-scheme";
        } catch (Exception e) {
            return "redirect:/admin/schemes?error=Error loading scheme";
        }
    }

    @PostMapping("/schemes/edit/{id}")
    public String updateScheme(@PathVariable Long id, 
                              @ModelAttribute SchemeDto schemeDto, 
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating scheme ID: " + id);
            
            // Validate required fields
            if (schemeDto.getTitle() == null || schemeDto.getTitle().trim().isEmpty()) {
                model.addAttribute("error", "Scheme title is required");
                List<Category> categories = categoryRepository.findAll();
                model.addAttribute("categories", categories);
                return "admin/edit-scheme";
            }
            
            if (schemeDto.getCategoryId() == null) {
                model.addAttribute("error", "Category is required");
                List<Category> categories = categoryRepository.findAll();
                model.addAttribute("categories", categories);
                return "admin/edit-scheme";
            }
            
            schemeService.updateScheme(id, schemeDto);
            redirectAttributes.addFlashAttribute("success", "Scheme updated successfully!");
            return "redirect:/admin/schemes?success=true";
        } catch (IOException e) {
            model.addAttribute("error", "Error uploading files: " + e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "admin/edit-scheme";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating scheme: " + e.getMessage());
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "admin/edit-scheme";
        }
    }

    @GetMapping("/schemes/delete/{id}")
    public String deleteScheme(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Deleting scheme ID: " + id);
            
            Scheme scheme = schemeService.getSchemeById(id);
            if (scheme == null) {
                redirectAttributes.addFlashAttribute("error", "Scheme not found");
                return "redirect:/admin/schemes";
            }
            
            schemeService.deleteScheme(id);
            redirectAttributes.addFlashAttribute("success", "Scheme deleted successfully!");
            return "redirect:/admin/schemes?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting scheme: " + e.getMessage());
            return "redirect:/admin/schemes";
        }
    }

    @PostMapping("/schemes/toggle/{id}")
    public String toggleSchemeStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Scheme scheme = schemeService.getSchemeById(id);
            if (scheme != null) {
                scheme.setActive(!scheme.isActive());
                schemeService.saveScheme(scheme);
                
                String status = scheme.isActive() ? "activated" : "deactivated";
                redirectAttributes.addFlashAttribute("success", "Scheme " + status + " successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Scheme not found");
            }
            return "redirect:/admin/schemes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating scheme status: " + e.getMessage());
            return "redirect:/admin/schemes";
        }
    }
	/*
	 * @GetMapping("/categories") public String manageCategories(Model model) { try
	 * { List<Category> categories = categoryRepository.findAll();
	 * model.addAttribute("categories", categories); return "admin/categories"; }
	 * catch (Exception e) { model.addAttribute("error",
	 * "Error loading categories: " + e.getMessage()); return "admin/categories"; }
	 * }
	 */

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name, 
                            @RequestParam String level,
                            RedirectAttributes redirectAttributes) {
        try {
            // Check if category already exists
            List<Category> existingCategories = categoryRepository.findByNameContainingIgnoreCase(name);
            if (!existingCategories.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Category with this name already exists");
                return "redirect:/admin/categories";
            }
            
            Category category = new Category();
            category.setName(name);
            category.setLevel(level);
            categoryRepository.save(category);
            
            redirectAttributes.addFlashAttribute("success", "Category added successfully!");
            return "redirect:/admin/categories?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding category: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Check if category has schemes
            if (!category.getSchemes().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete category. It has " + category.getSchemes().size() + " schemes associated.");
                return "redirect:/admin/categories";
            }
            
            categoryRepository.delete(category);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
            return "redirect:/admin/categories?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting category: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

  

    // Quick actions for admin
    @GetMapping("/quick-actions")
    public String quickActions() {
        return "admin/quick-actions";
    }

    // Helper method for converting Scheme to SchemeDto
    private SchemeDto convertToDto(Scheme scheme) {
        SchemeDto dto = new SchemeDto();
        dto.setId(scheme.getId());
        dto.setTitle(scheme.getTitle());
        dto.setCategoryId(scheme.getCategory().getId());
        dto.setLevel(scheme.getLevel());
        dto.setShortDesc(scheme.getShortDesc());
        dto.setFullDesc(scheme.getFullDesc());
        dto.setApplyLink(scheme.getApplyLink());
        dto.setVideoUrl(scheme.getVideoUrl());
        dto.setBlogContent(scheme.getBlogContent());
        dto.setImageUrl(scheme.getImageUrl());
        dto.setPdfUrl(scheme.getPdfUrl());
        return dto;
    }

    // Debug endpoint for admin
    @GetMapping("/debug")
    @ResponseBody
    public String adminDebug() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== ADMIN DEBUG INFO ===\n");
        debugInfo.append("User: ").append(auth.getName()).append("\n");
        debugInfo.append("Authorities: ").append(auth.getAuthorities()).append("\n");
        debugInfo.append("Authenticated: ").append(auth.isAuthenticated()).append("\n");
        debugInfo.append("Total Schemes: ").append(schemeRepository.count()).append("\n");
        debugInfo.append("Total Categories: ").append(categoryRepository.count()).append("\n");
        
        return debugInfo.toString();
    }
    
    
 // Add this method to AdminController.java
    @GetMapping("/statistics")
    public String statistics(Model model) {
        try {
            List<Scheme> allSchemes = schemeService.getAllSchemes();
            List<Category> categories = categoryRepository.findAll();
            
            long totalSchemes = allSchemes.size();
            long activeSchemes = allSchemes.stream().filter(Scheme::isActive).count();
            long inactiveSchemes = totalSchemes - activeSchemes;
            
            // Count schemes by level
            long centralSchemes = allSchemes.stream().filter(s -> "CENTRAL".equals(s.getLevel())).count();
            long stateSchemes = allSchemes.stream().filter(s -> "STATE".equals(s.getLevel())).count();
            long districtSchemes = allSchemes.stream().filter(s -> "DISTRICT".equals(s.getLevel())).count();
            long privateSchemes = allSchemes.stream().filter(s -> "PRIVATE".equals(s.getLevel())).count();
            
            // Calculate total shares
            int totalShares = allSchemes.stream().mapToInt(Scheme::getShareCount).sum();
            
            // Get most shared schemes
            List<Scheme> topSharedSchemes = schemeRepository.findTopSharedSchemes().stream()
                    .limit(5)
                    .collect(Collectors.toList());
            
            model.addAttribute("totalSchemes", totalSchemes);
            model.addAttribute("activeSchemes", activeSchemes);
            model.addAttribute("inactiveSchemes", inactiveSchemes);
            model.addAttribute("centralSchemes", centralSchemes);
            model.addAttribute("stateSchemes", stateSchemes);
            model.addAttribute("districtSchemes", districtSchemes);
            model.addAttribute("privateSchemes", privateSchemes);
            model.addAttribute("totalShares", totalShares);
            model.addAttribute("topSharedSchemes", topSharedSchemes);
            model.addAttribute("categories", categories);
            
            return "admin/statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading statistics: " + e.getMessage());
            return "admin/statistics";
        }
    }
    
    
 // AdminController.java में यह method add करें
    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "admin/users";
        }
    }
    
    
 // AdminController.java में ये methods add करें

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, 
                            @RequestParam String username,
                            @RequestParam String email,
                            @RequestParam String role,
                            @RequestParam(required = false) Boolean enabled,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            
            if (enabled != null) {
                user.setEnabled(enabled);
            }
            
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            return "redirect:/admin/users?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Prevent deleting own account
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (user.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
                return "redirect:/admin/users";
            }
            
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            return "redirect:/admin/users?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

 // AdminController.java में यह method update करें
    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Prevent deactivating own account
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (user.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "You cannot deactivate your own account!");
                return "redirect:/admin/users";
            }
            
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            
            String status = user.isEnabled() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "User " + status + " successfully!");
            return "redirect:/admin/users?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    
 // AdminController में यह method update करें
    @GetMapping("/categories")
    public String manageCategoriString(Model model) {
        try {
            List<Category> categories = categoryRepository.findAll();
            
            // Count schemes for each category
            Map<Long, Long> schemeCounts = new HashMap<>();
            for (Category category : categories) {
                long count = schemeRepository.countByCategoryId(category.getId());
                schemeCounts.put(category.getId(), count);
            }
            
            model.addAttribute("categories", categories);
            model.addAttribute("schemeCounts", schemeCounts);
            return "admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading categories: " + e.getMessage());
            return "admin/categories";
        }
    }

//    // Add category with better validation
//    @PostMapping("/categories/add")
//    public String addCategory(@RequestParam String name, 
//                            @RequestParam String level,
//                            RedirectAttributes redirectAttributes) {
//        try {
//            // Validate inputs
//            if (name == null || name.trim().isEmpty()) {
//                redirectAttributes.addFlashAttribute("error", "Category name is required");
//                return "redirect:/admin/categories";
//            }
//            
//            if (level == null || level.trim().isEmpty()) {
//                redirectAttributes.addFlashAttribute("error", "Category level is required");
//                return "redirect:/admin/categories";
//            }
//            
//            // Check if category already exists
//            List<Category> existingCategories = categoryRepository.findByNameContainingIgnoreCase(name.trim());
//            if (!existingCategories.isEmpty()) {
//                redirectAttributes.addFlashAttribute("error", "Category with this name already exists");
//                return "redirect:/admin/categories";
//            }
//            
//            Category category = new Category();
//            category.setName(name.trim());
//            category.setLevel(level);
//            categoryRepository.save(category);
//            
//            redirectAttributes.addFlashAttribute("success", "Category added successfully!");
//            return "redirect:/admin/categories?success=true";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error adding category: " + e.getMessage());
//            return "redirect:/admin/categories";
//        }
//    }
    
}