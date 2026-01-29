package com.empowerher.controllers;

import com.empowerher.dto.SchemeDto;
import com.empowerher.entities.Scheme;
import com.empowerher.services.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@CrossOrigin(origins = "*")
public class SchemeController {

    @Autowired
    private SchemeService schemeService;

    // Get all active schemes
    @GetMapping
    public ResponseEntity<List<Scheme>> getAllSchemes() {
        List<Scheme> schemes = schemeService.getActiveSchemes();
        return ResponseEntity.ok(schemes);
    }

    // Get scheme by ID
    @GetMapping("/{id}")
    public ResponseEntity<Scheme> getSchemeById(@PathVariable Long id) {
        Scheme scheme = schemeService.getSchemeById(id);
        if (scheme != null) {
            return ResponseEntity.ok(scheme);
        }
        return ResponseEntity.notFound().build();
    }

    // Create new scheme
    @PostMapping
    public ResponseEntity<Scheme> createScheme(@ModelAttribute SchemeDto schemeDto) {
        try {
            Scheme createdScheme = schemeService.createScheme(schemeDto);
            return ResponseEntity.ok(createdScheme);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update scheme
    @PutMapping("/{id}")
    public ResponseEntity<Scheme> updateScheme(
            @PathVariable Long id, 
            @ModelAttribute SchemeDto schemeDto) {
        try {
            Scheme updatedScheme = schemeService.updateScheme(id, schemeDto);
            return ResponseEntity.ok(updatedScheme);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete scheme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable Long id) {
        try {
            schemeService.deleteScheme(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Search schemes
    @GetMapping("/search")
    public ResponseEntity<List<Scheme>> searchSchemes(@RequestParam String query) {
        List<Scheme> schemes = schemeService.searchSchemes(query);
        return ResponseEntity.ok(schemes);
    }

    // Get schemes by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Scheme>> getSchemesByCategory(@PathVariable Long categoryId) {
        List<Scheme> schemes = schemeService.getSchemesByCategory(categoryId);
        return ResponseEntity.ok(schemes);
    }

    // Get schemes by level
    @GetMapping("/level/{level}")
    public ResponseEntity<List<Scheme>> getSchemesByLevel(@PathVariable String level) {
        List<Scheme> schemes = schemeService.getSchemesByLevel(level);
        return ResponseEntity.ok(schemes);
    }
}