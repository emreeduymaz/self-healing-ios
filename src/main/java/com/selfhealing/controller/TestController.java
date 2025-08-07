package com.selfhealing.controller;

import com.selfhealing.model.TestElement;
import com.selfhealing.service.ElementComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for testing algorithms and comparisons
 */
@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private ElementComparisonService comparisonService;

    /**
     * Test string similarity algorithms
     * POST /api/v1/test/string-similarity
     */
    @PostMapping("/string-similarity")
    public ResponseEntity<Map<String, Object>> testStringSimilarity(@RequestBody Map<String, String> request) {
        String str1 = request.get("string1");
        String str2 = request.get("string2");
        
        if (str1 == null || str2 == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Both string1 and string2 are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        double similarity = comparisonService.compareStrings(str1, str2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("string1", str1);
        response.put("string2", str2);
        response.put("similarity", similarity);
        response.put("similarityPercentage", String.format("%.2f%%", similarity * 100));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test element comparison
     * POST /api/v1/test/element-similarity
     */
    @PostMapping("/element-similarity")
    public ResponseEntity<Map<String, Object>> testElementSimilarity(@RequestBody Map<String, TestElement> request) {
        TestElement element1 = request.get("element1");
        TestElement element2 = request.get("element2");
        
        if (element1 == null || element2 == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Both element1 and element2 are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        double similarity = comparisonService.compareElements(element1, element2);
        boolean isExactMatch = comparisonService.isExactMatch(element1, element2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("element1", element1);
        response.put("element2", element2);
        response.put("similarity", similarity);
        response.put("similarityPercentage", String.format("%.2f%%", similarity * 100));
        response.put("isExactMatch", isExactMatch);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test XPath comparison specifically
     * POST /api/v1/test/xpath-similarity
     */
    @PostMapping("/xpath-similarity")
    public ResponseEntity<Map<String, Object>> testXPathSimilarity(@RequestBody Map<String, String> request) {
        String xpath1 = request.get("xpath1");
        String xpath2 = request.get("xpath2");
        
        if (xpath1 == null || xpath2 == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Both xpath1 and xpath2 are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        double similarity = comparisonService.compareXPaths(xpath1, xpath2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("xpath1", xpath1);
        response.put("xpath2", xpath2);
        response.put("similarity", similarity);
        response.put("similarityPercentage", String.format("%.2f%%", similarity * 100));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a sample test element for testing
     * GET /api/v1/test/sample-element
     */
    @GetMapping("/sample-element")
    public ResponseEntity<TestElement> getSampleElement() {
        TestElement sampleElement = new TestElement(
            "test_login_button",
            "//XCUIElementTypeButton[@name='loginButton']",
            "loginButton",
            "XCUIElementTypeButton",
            "loginButton",
            "LoginScreen",
            "button"
        );
        
        return ResponseEntity.ok(sampleElement);
    }

    /**
     * Test enhanced string similarity with dynamic threshold
     * POST /api/v1/test/enhanced-similarity
     */
    @PostMapping("/enhanced-similarity")
    public ResponseEntity<Map<String, Object>> testEnhancedSimilarity(@RequestBody Map<String, String> request) {
        String str1 = request.get("string1");
        String str2 = request.get("string2");
        
        if (str1 == null || str2 == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Both string1 and string2 are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Get enhanced string matcher from comparison service
        double enhancedSimilarity = comparisonService.compareStringsEnhanced(str1, str2, 0.75);
        double traditionalSimilarity = comparisonService.compareStrings(str1, str2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("string1", str1);
        response.put("string2", str2);
        response.put("enhancedSimilarity", enhancedSimilarity);
        response.put("enhancedPercentage", String.format("%.2f%%", enhancedSimilarity * 100));
        response.put("traditionalSimilarity", traditionalSimilarity);
        response.put("traditionalPercentage", String.format("%.2f%%", traditionalSimilarity * 100));
        response.put("improvement", enhancedSimilarity - traditionalSimilarity);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get multiple sample elements for testing
     * GET /api/v1/test/sample-elements
     */
    @GetMapping("/sample-elements")
    public ResponseEntity<Map<String, TestElement>> getSampleElements() {
        Map<String, TestElement> samples = new HashMap<>();
        
        samples.put("loginButton", new TestElement(
            "login_submit_button",
            "//XCUIElementTypeButton[@name='loginButton']",
            "loginButton",
            "XCUIElementTypeButton",
            "loginButton",
            "LoginScreen",
            "button"
        ));
        
        samples.put("emailField", new TestElement(
            "login_email_field",
            "//XCUIElementTypeTextField[@name='emailTextField']",
            "emailTextField",
            "XCUIElementTypeTextField",
            "emailTextField",
            "LoginScreen",
            "textfield"
        ));
        
        samples.put("passwordField", new TestElement(
            "login_password_field",
            "//XCUIElementTypeSecureTextField[@name='passwordSecureTextField']",
            "passwordSecureTextField",
            "XCUIElementTypeSecureTextField",
            "passwordSecureTextField",
            "LoginScreen",
            "securetextfield"
        ));
        
        return ResponseEntity.ok(samples);
    }
} 