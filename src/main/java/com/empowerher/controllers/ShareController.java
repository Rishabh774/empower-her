package com.empowerher.controllers;

import com.empowerher.entities.Scheme;
import com.empowerher.repositories.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired
    private SchemeRepository schemeRepository;

    @PostMapping("/increment/{schemeId}")
    public ResponseEntity<Map<String, Object>> incrementShareCount(@PathVariable Long schemeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Scheme scheme = schemeRepository.findById(schemeId)
                    .orElseThrow(() -> new RuntimeException("Scheme not found"));
            
            scheme.incrementShareCount();
            schemeRepository.save(scheme);
            
            response.put("success", true);
            response.put("shareCount", scheme.getShareCount());
            response.put("message", "Share count updated");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/count/{schemeId}")
    public ResponseEntity<Map<String, Object>> getShareCount(@PathVariable Long schemeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Scheme scheme = schemeRepository.findById(schemeId)
                    .orElseThrow(() -> new RuntimeException("Scheme not found"));
            
            response.put("success", true);
            response.put("shareCount", scheme.getShareCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}