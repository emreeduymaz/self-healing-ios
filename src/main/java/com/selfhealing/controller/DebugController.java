package com.selfhealing.controller;

import com.selfhealing.algorithm.EnhancedStringMatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller to test algorithms directly
 */
@RestController
@RequestMapping("/api/v1/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private EnhancedStringMatcher enhancedStringMatcher;



    /**
     * Test string similarity directly
     */
    @PostMapping("/string-similarity")
    public ResponseEntity<Map<String, Object>> testStringSimilarity(@RequestBody Map<String, String> request) {
        String str1 = request.get("str1");
        String str2 = request.get("str2");
        
        double similarity = enhancedStringMatcher.calculateEnhancedSimilarity(str1, str2);
        double threshold = enhancedStringMatcher.calculateDynamicThreshold(str1, str2, 0.75);
        
        Map<String, Object> response = new HashMap<>();
        response.put("str1", str1);
        response.put("str2", str2);
        response.put("similarity", similarity);
        response.put("dynamicThreshold", threshold);
        response.put("wouldMatch", similarity >= threshold);
        
        return ResponseEntity.ok(response);
    }
}