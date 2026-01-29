package com.empowerher.controllers;

import com.empowerher.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/admin/test-email")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String testEmail(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            emailService.sendTestEmail(email);
            redirectAttributes.addFlashAttribute("success", "Test email sent successfully to: " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send test email: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}