package com.selfhealing.controller;

import com.selfhealing.model.SimilarityResult;
import com.selfhealing.model.TestElement;
import com.selfhealing.service.SelfHealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Self-Healing functionality
 */
@RestController
@RequestMapping("/api/v1/self-healing")
@CrossOrigin(origins = "*")
public class SelfHealingController {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingController.class);

    @Autowired
    private SelfHealingService selfHealingService;

    /**
     * Find an element by ID or similarity
     * POST /api/v1/self-healing/find
     */
    @PostMapping("/find")
    public ResponseEntity<Map<String, Object>> findElement(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding element: {}", targetElement.getElementId());
            
            // Validate element
            List<String> validationErrors = selfHealingService.validateElement(targetElement);
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            SimilarityResult result = selfHealingService.findElement(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            
            if ("EXACT".equals(result.getMatchType())) {
                response.put("message", "Element found - exact match");
            } else if ("SIMILARITY".equals(result.getMatchType())) {
                response.put("message", String.format("Element found - similarity match (%.2f%%)", 
                    result.getSimilarityScore() * 100));
            } else {
                response.put("message", "Element not found - no suitable match");
            }

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding element", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get suggestions for an element
     * POST /api/v1/self-healing/suggestions
     */
    @PostMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions(@RequestBody TestElement targetElement) {
        try {
            logger.info("Getting suggestions for element: {}", targetElement.getElementId());
            
            List<SimilarityResult> suggestions = selfHealingService.getSuggestions(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suggestions", suggestions);
            response.put("count", suggestions.size());
            response.put("message", String.format("Found %d suggestions", suggestions.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting suggestions", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an element
     * PUT /api/v1/self-healing/update/{oldElementId}
     */
    @PutMapping("/update/{oldElementId}")
    public ResponseEntity<Map<String, Object>> updateElement(
            @PathVariable String oldElementId,
            @RequestBody TestElement newElement) {
        try {
            logger.info("Updating element: {} -> {}", oldElementId, newElement.getElementId());
            
            // Validate new element
            List<String> validationErrors = selfHealingService.validateElement(newElement);
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean updated = selfHealingService.updateElement(oldElementId, newElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", updated);
            
            if (updated) {
                response.put("message", String.format("Element updated successfully: %s -> %s", 
                    oldElementId, newElement.getElementId()));
            } else {
                response.put("message", "Element not found for update: " + oldElementId);
            }

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating element", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Validate an element
     * POST /api/v1/self-healing/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateElement(@RequestBody TestElement element) {
        try {
            List<String> validationErrors = selfHealingService.validateElement(element);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", validationErrors.isEmpty());
            response.put("errors", validationErrors);
            
            if (validationErrors.isEmpty()) {
                response.put("message", "Element is valid");
            } else {
                response.put("message", "Element validation failed");
            }

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error validating element", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get system statistics
     * GET /api/v1/self-healing/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = selfHealingService.getStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            response.put("message", "Statistics retrieved successfully");

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/self-healing/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Self-Healing service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "self-healing-lsc-ios");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Configuration endpoint
     * GET /api/v1/self-healing/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("similarityThreshold", selfHealingService.getSimilarityThreshold());
        config.put("autoUpdateEnabled", selfHealingService.isAutoUpdateEnabled());
        config.put("maxSuggestions", selfHealingService.getMaxSuggestions());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configuration", config);
        response.put("message", "Configuration retrieved successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Find elements by xpath only
     * POST /api/v1/self-healing/find-by-xpath
     */
    @PostMapping("/find-by-xpath")
    public ResponseEntity<Map<String, Object>> findByXpath(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding elements by xpath: {}", targetElement.getXpath());
            
            List<SimilarityResult> results = selfHealingService.findByXpath(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("message", String.format("Found %d xpath matches", results.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding by xpath", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find elements by accessibility_id only
     * POST /api/v1/self-healing/find-by-accessibility-id
     */
    @PostMapping("/find-by-accessibility-id")
    public ResponseEntity<Map<String, Object>> findByAccessibilityId(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding elements by accessibility_id: {}", targetElement.getAccessibilityId());
            
            List<SimilarityResult> results = selfHealingService.findByAccessibilityId(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("message", String.format("Found %d accessibility_id matches", results.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding by accessibility_id", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find elements by element_id only
     * POST /api/v1/self-healing/find-by-element-id
     */
    @PostMapping("/find-by-element-id")
    public ResponseEntity<Map<String, Object>> findByElementId(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding elements by element_id: {}", targetElement.getElementId());
            
            List<SimilarityResult> results = selfHealingService.findByElementId(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("message", String.format("Found %d element_id matches", results.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding by element_id", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find elements by class_name only
     * POST /api/v1/self-healing/find-by-class-name
     */
    @PostMapping("/find-by-class-name")
    public ResponseEntity<Map<String, Object>> findByClassName(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding elements by class_name: {}", targetElement.getClassName());
            
            List<SimilarityResult> results = selfHealingService.findByClassName(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("message", String.format("Found %d class_name matches", results.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding by class_name", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find elements by name only
     * POST /api/v1/self-healing/find-by-name
     */
    @PostMapping("/find-by-name")
    public ResponseEntity<Map<String, Object>> findByName(@RequestBody TestElement targetElement) {
        try {
            logger.info("Finding elements by name: {}", targetElement.getName());
            
            List<SimilarityResult> results = selfHealingService.findByName(targetElement);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("message", String.format("Found %d name matches", results.size()));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error finding by name", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 