package com.selfhealing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selfhealing.model.ElementsData;
import com.selfhealing.model.SimilarityResult;
import com.selfhealing.model.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main Self-Healing Service that manages element matching and updating
 */
@Service
public class SelfHealingService {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingService.class);

    @Autowired
    private ElementComparisonService comparisonService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${selfhealing.similarity.threshold:0.75}")
    private double similarityThreshold;

    @Value("${selfhealing.auto-update.enabled:true}")
    private boolean autoUpdateEnabled;

    @Value("${selfhealing.max-suggestions:5}")
    private int maxSuggestions;

    private ElementsData cachedElementsData;
    private long lastLoadTime = 0;
    private static final long CACHE_DURATION = 60000; // 1 minute cache

    /**
     * Load elements data from JSON file
     * @return ElementsData object
     */
    private ElementsData loadElementsData() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Return cached data if still valid
            if (cachedElementsData != null && (currentTime - lastLoadTime) < CACHE_DURATION) {
                return cachedElementsData;
            }

            logger.debug("Loading elements data from JSON file");
            ClassPathResource resource = new ClassPathResource("elements.json");
            
            try (InputStream inputStream = resource.getInputStream()) {
                cachedElementsData = objectMapper.readValue(inputStream, ElementsData.class);
                lastLoadTime = currentTime;
                logger.info("Loaded {} test elements", cachedElementsData.getTestElements().size());
                return cachedElementsData;
            }
        } catch (IOException e) {
            logger.error("Failed to load elements data", e);
            throw new RuntimeException("Failed to load elements data", e);
        }
    }

    /**
     * Find element by ID or similar characteristics
     * @param targetElement Element to search for
     * @return SimilarityResult with match information
     */
    public SimilarityResult findElement(TestElement targetElement) {
        if (targetElement == null) {
            return createNotFoundResult(targetElement, "Invalid target element");
        }
        
        // Check if at least one identifier is present
        boolean hasElementId = targetElement.getElementId() != null && !targetElement.getElementId().trim().isEmpty();
        boolean hasXpath = targetElement.getXpath() != null && !targetElement.getXpath().trim().isEmpty();
        boolean hasAccessibilityId = targetElement.getAccessibilityId() != null && !targetElement.getAccessibilityId().trim().isEmpty();
        boolean hasClassName = targetElement.getClassName() != null && !targetElement.getClassName().trim().isEmpty();
        boolean hasName = targetElement.getName() != null && !targetElement.getName().trim().isEmpty();
        
        if (!hasElementId && !hasXpath && !hasAccessibilityId && !hasClassName && !hasName) {
            return createNotFoundResult(targetElement, "No identifier fields provided");
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Searching for element with available fields");

        // Step 1: Try exact ID match if element_id is provided
        if (hasElementId) {
            Optional<TestElement> exactMatch = allElements.stream()
                    .filter(element -> targetElement.getElementId().equals(element.getElementId()))
                    .findFirst();

            if (exactMatch.isPresent()) {
                logger.debug("Found exact ID match for: {}", targetElement.getElementId());
                return new SimilarityResult(
                    targetElement, 
                    exactMatch.get(), 
                    1.0, 
                    "EXACT", 
                    false
                );
            }
        }

        // Step 2: Try exact attribute match (accessibility_id, name, xpath)
        Optional<TestElement> exactAttributeMatch = allElements.stream()
                .filter(element -> comparisonService.isExactMatch(targetElement, element))
                .findFirst();

        if (exactAttributeMatch.isPresent()) {
            logger.debug("Found exact attribute match for: {}", targetElement.getElementId());
            return createAutoUpdateResult(targetElement, exactAttributeMatch.get(), 1.0);
        }

        // Step 3: Find similar elements using enhanced algorithms with dynamic threshold
        // Use much lower threshold for better near-miss detection
        List<Map.Entry<TestElement, Double>> similarElements = comparisonService.findBestMatchesEnhanced(
            targetElement, 
            allElements, 
            0.1, // Much lower base threshold
            true // Use dynamic threshold
        );

        if (similarElements.isEmpty()) {
            logger.info("No similar elements found for element. Threshold: {}, Available fields: element_id={}, xpath={}, accessibility_id={}, className={}, name={}", 
                similarityThreshold, hasElementId, hasXpath, hasAccessibilityId, hasClassName, hasName);
            return createNotFoundResult(targetElement, "No similar elements found above minimum threshold");
        }

        // Return the best match
        Map.Entry<TestElement, Double> bestMatch = similarElements.get(0);
        logger.debug("Found similar element for: {} -> {} (similarity: {})", 
            targetElement.getElementId(), 
            bestMatch.getKey().getElementId(), 
            bestMatch.getValue());

        return createAutoUpdateResult(targetElement, bestMatch.getKey(), bestMatch.getValue());
    }

    /**
     * Get multiple suggestions for an element
     * @param targetElement Element to search for
     * @return List of similarity results sorted by similarity
     */
    public List<SimilarityResult> getSuggestions(TestElement targetElement) {
        if (targetElement == null) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Getting suggestions for element: {}", targetElement.getElementId());

        List<Map.Entry<TestElement, Double>> similarElements = comparisonService.findBestMatchesEnhanced(
            targetElement, 
            allElements, 
            0.15, // Much lower threshold for suggestions to catch near-misses
            true // Use dynamic threshold
        );

        return similarElements.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "SIMILARITY" : "LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Update elements data with new element information
     * @param oldElementId ID of element to replace
     * @param newElement New element data
     * @return true if update was successful
     */
    public boolean updateElement(String oldElementId, TestElement newElement) {
        if (oldElementId == null || newElement == null) {
            return false;
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Updating element: {} -> {}", oldElementId, newElement.getElementId());

        // Find and replace the element
        for (int i = 0; i < allElements.size(); i++) {
            TestElement element = allElements.get(i);
            if (oldElementId.equals(element.getElementId())) {
                allElements.set(i, newElement);
                
                // Reset cache to force reload
                cachedElementsData = null;
                
                logger.info("Updated element: {} -> {}", oldElementId, newElement.getElementId());
                return true;
            }
        }

        logger.warn("Element not found for update: {}", oldElementId);
        return false;
    }

    /**
     * Get statistics about the current elements database
     * @return Map with various statistics
     */
    public Map<String, Object> getStatistics() {
        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalElements", allElements.size());
        stats.put("similarityThreshold", similarityThreshold);
        stats.put("autoUpdateEnabled", autoUpdateEnabled);
        stats.put("maxSuggestions", maxSuggestions);

        // Count by screen
        Map<String, Long> screenCounts = allElements.stream()
                .collect(Collectors.groupingBy(
                    element -> element.getScreen() != null ? element.getScreen() : "Unknown",
                    Collectors.counting()
                ));
        stats.put("elementsByScreen", screenCounts);

        // Count by element type
        Map<String, Long> typeCounts = allElements.stream()
                .collect(Collectors.groupingBy(
                    element -> element.getElementType() != null ? element.getElementType() : "Unknown",
                    Collectors.counting()
                ));
        stats.put("elementsByType", typeCounts);

        return stats;
    }

    /**
     * Validate the integrity of an element
     * @param element Element to validate
     * @return List of validation errors (empty if valid)
     */
    public List<String> validateElement(TestElement element) {
        List<String> errors = new ArrayList<>();

        if (element == null) {
            errors.add("Element is null");
            return errors;
        }

        // Check if at least one identifier is provided
        boolean hasElementId = element.getElementId() != null && !element.getElementId().trim().isEmpty();
        boolean hasXpath = element.getXpath() != null && !element.getXpath().trim().isEmpty();
        boolean hasAccessibilityId = element.getAccessibilityId() != null && !element.getAccessibilityId().trim().isEmpty();
        boolean hasClassName = element.getClassName() != null && !element.getClassName().trim().isEmpty();
        
        if (!hasElementId && !hasXpath && !hasAccessibilityId && !hasClassName) {
            errors.add("At least one identifier is required (element_id, xpath, accessibility_id, or class_name)");
        }

        return errors;
    }

    /**
     * Create a result for elements that were not found
     */
    private SimilarityResult createNotFoundResult(TestElement targetElement, String reason) {
        SimilarityResult result = new SimilarityResult();
        result.setOriginalElement(targetElement);
        result.setMatchedElement(null);
        result.setSimilarityScore(0.0);
        result.setMatchType("NOT_FOUND");
        result.setAutoUpdated(false);
        return result;
    }

    /**
     * Create a result for auto-update scenarios
     */
    private SimilarityResult createAutoUpdateResult(TestElement original, TestElement matched, double similarity) {
        boolean shouldAutoUpdate = autoUpdateEnabled && similarity >= similarityThreshold;
        
        if (shouldAutoUpdate && !original.getElementId().equals(matched.getElementId())) {
            // Perform auto-update
            updateElement(original.getElementId(), matched);
        }

        return new SimilarityResult(
            original,
            matched,
            similarity,
            similarity >= similarityThreshold ? "SIMILARITY" : "LOW_SIMILARITY",
            shouldAutoUpdate
        );
    }

    // Getters for configuration values (for testing)
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public int getMaxSuggestions() {
        return maxSuggestions;
    }

    /**
     * Find element matches by xpath only
     * @param targetElement Element with xpath to search for
     * @return List of similarity results based on xpath matching
     */
    public List<SimilarityResult> findByXpath(TestElement targetElement) {
        if (targetElement == null || targetElement.getXpath() == null || targetElement.getXpath().trim().isEmpty()) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Finding elements by xpath: {}", targetElement.getXpath());

        List<Map.Entry<TestElement, Double>> matches = comparisonService.findMatchesByXpath(
            targetElement, 
            allElements, 
            0.15 // Much lower threshold to catch near-misses
        );

        return matches.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "XPATH_MATCH" : "XPATH_LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Find element matches by accessibility_id only
     * @param targetElement Element with accessibility_id to search for
     * @return List of similarity results based on accessibility_id matching
     */
    public List<SimilarityResult> findByAccessibilityId(TestElement targetElement) {
        if (targetElement == null || targetElement.getAccessibilityId() == null || targetElement.getAccessibilityId().trim().isEmpty()) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Finding elements by accessibility_id: {}", targetElement.getAccessibilityId());

        List<Map.Entry<TestElement, Double>> matches = comparisonService.findMatchesByAccessibilityId(
            targetElement, 
            allElements, 
            0.15 // Much lower threshold to catch near-misses
        );

        return matches.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "ACCESSIBILITY_ID_MATCH" : "ACCESSIBILITY_ID_LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Find element matches by element_id only
     * @param targetElement Element with element_id to search for
     * @return List of similarity results based on element_id matching
     */
    public List<SimilarityResult> findByElementId(TestElement targetElement) {
        if (targetElement == null || targetElement.getElementId() == null || targetElement.getElementId().trim().isEmpty()) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Finding elements by element_id: {}", targetElement.getElementId());

        List<Map.Entry<TestElement, Double>> matches = comparisonService.findMatchesByElementId(
            targetElement, 
            allElements, 
            0.15 // Much lower threshold to catch near-misses
        );

        return matches.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "ELEMENT_ID_MATCH" : "ELEMENT_ID_LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Find element matches by class_name only
     * @param targetElement Element with class_name to search for
     * @return List of similarity results based on class_name matching
     */
    public List<SimilarityResult> findByClassName(TestElement targetElement) {
        if (targetElement == null || targetElement.getClassName() == null || targetElement.getClassName().trim().isEmpty()) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Finding elements by class_name: {}", targetElement.getClassName());

        List<Map.Entry<TestElement, Double>> matches = comparisonService.findMatchesByClassName(
            targetElement, 
            allElements, 
            0.15 // Much lower threshold to catch near-misses
        );

        return matches.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "CLASS_NAME_MATCH" : "CLASS_NAME_LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Find element matches by name only
     * @param targetElement Element with name to search for
     * @return List of similarity results based on name matching
     */
    public List<SimilarityResult> findByName(TestElement targetElement) {
        if (targetElement == null || targetElement.getName() == null || targetElement.getName().trim().isEmpty()) {
            return Collections.emptyList();
        }

        ElementsData elementsData = loadElementsData();
        List<TestElement> allElements = elementsData.getTestElements();

        logger.debug("Finding elements by name: {}", targetElement.getName());

        List<Map.Entry<TestElement, Double>> matches = comparisonService.findMatchesByName(
            targetElement, 
            allElements, 
            0.15 // Much lower threshold to catch near-misses
        );

        return matches.stream()
                .limit(maxSuggestions)
                .map(entry -> new SimilarityResult(
                    targetElement,
                    entry.getKey(),
                    entry.getValue(),
                    entry.getValue() >= similarityThreshold ? "NAME_MATCH" : "NAME_LOW_SIMILARITY",
                    false
                ))
                .collect(Collectors.toList());
    }
} 