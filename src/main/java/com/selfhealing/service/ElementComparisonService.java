package com.selfhealing.service;

import com.selfhealing.algorithm.LevenshteinDistance;
import com.selfhealing.algorithm.LongestCommonSubsequence;
import com.selfhealing.algorithm.EnhancedStringMatcher;
import com.selfhealing.model.TestElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Collections;

/**
 * Service for comparing test elements using Levenshtein and LCS algorithms
 */
@Service
public class ElementComparisonService {

    @Autowired
    private LevenshteinDistance levenshteinDistance;
    
    @Autowired
    private LongestCommonSubsequence longestCommonSubsequence;
    
    @Autowired
    private EnhancedStringMatcher enhancedStringMatcher;

    /**
     * Compare two test elements and calculate overall similarity
     * @param element1 First test element
     * @param element2 Second test element
     * @return Similarity score (0.0 to 1.0)
     */
    public double compareElements(TestElement element1, TestElement element2) {
        return compareElementsWithThreshold(element1, element2, 0.75);
    }

    /**
     * Compare two test elements with dynamic threshold calculation
     * Only compares fields that are present in both elements
     * @param element1 First test element
     * @param element2 Second test element
     * @param baseThreshold Base threshold for comparison
     * @return Similarity score (0.0 to 1.0)
     */
    public double compareElementsWithThreshold(TestElement element1, TestElement element2, double baseThreshold) {
        if (element1 == null || element2 == null) {
            return 0.0;
        }

        double totalSimilarity = 0.0;
        double totalWeight = 0.0;

        // Base weight factors - will be normalized based on available fields
        double accessibilityIdWeight = 0.30; // Increased for iOS
        double nameWeight = 0.25;
        double xpathWeight = 0.18;
        double classNameWeight = 0.12;
        double screenWeight = 0.10;
        double elementTypeWeight = 0.05;

        // Compare accessibility_id only if both elements have it
        if (hasValue(element1.getAccessibilityId()) && hasValue(element2.getAccessibilityId())) {
            double accessibilityScore = compareStringsEnhanced(
                element1.getAccessibilityId(), 
                element2.getAccessibilityId(),
                baseThreshold
            );
            totalSimilarity += accessibilityIdWeight * accessibilityScore;
            totalWeight += accessibilityIdWeight;
        }

        // Compare name only if both elements have it
        if (hasValue(element1.getName()) && hasValue(element2.getName())) {
            double nameScore = compareStringsEnhanced(
                element1.getName(), 
                element2.getName(),
                baseThreshold
            );
            totalSimilarity += nameWeight * nameScore;
            totalWeight += nameWeight;
        }

        // Compare xpath only if both elements have it
        if (hasValue(element1.getXpath()) && hasValue(element2.getXpath())) {
            double xpathScore = compareXPathsEnhanced(
                element1.getXpath(), 
                element2.getXpath(),
                baseThreshold
            );
            totalSimilarity += xpathWeight * xpathScore;
            totalWeight += xpathWeight;
        }

        // Compare class_name only if both elements have it
        if (hasValue(element1.getClassName()) && hasValue(element2.getClassName())) {
            double classScore = compareStringsEnhanced(
                element1.getClassName(), 
                element2.getClassName(),
                baseThreshold
            );
            totalSimilarity += classNameWeight * classScore;
            totalWeight += classNameWeight;
        }

        // Compare screen only if both elements have it
        if (hasValue(element1.getScreen()) && hasValue(element2.getScreen())) {
            double screenScore = compareStringsEnhanced(
                element1.getScreen(), 
                element2.getScreen(),
                baseThreshold
            );
            totalSimilarity += screenWeight * screenScore;
            totalWeight += screenWeight;
        }

        // Compare element_type only if both elements have it
        if (hasValue(element1.getElementType()) && hasValue(element2.getElementType())) {
            double typeScore = compareStringsEnhanced(
                element1.getElementType(), 
                element2.getElementType(),
                baseThreshold
            );
            totalSimilarity += elementTypeWeight * typeScore;
            totalWeight += elementTypeWeight;
        }

        // If no matching fields found, return 0
        if (totalWeight == 0.0) {
            // Debug: This should not happen if we have proper field validation
            System.out.println("DEBUG: No matching fields found between elements!");
            System.out.println("Element1 - ID: " + element1.getElementId() + ", Xpath: " + element1.getXpath() + 
                ", AccessibilityId: " + element1.getAccessibilityId() + ", ClassName: " + element1.getClassName());
            System.out.println("Element2 - ID: " + element2.getElementId() + ", Xpath: " + element2.getXpath() + 
                ", AccessibilityId: " + element2.getAccessibilityId() + ", ClassName: " + element2.getClassName());
            return 0.0;
        }

        // Normalize the score based on actual weights used
        double normalizedScore = totalSimilarity / totalWeight;

        // Context bonuses (only if both elements have the fields)
        double contextBonus = calculateContextBonus(element1, element2);
        normalizedScore += contextBonus;

        return Math.max(0.0, Math.min(1.0, normalizedScore));
    }

    /**
     * Check if a string value is present and not empty
     * @param value String to check
     * @return true if value is not null and not empty after trimming
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Compare two strings using both Levenshtein and LCS algorithms
     * @param str1 First string
     * @param str2 Second string
     * @return Combined similarity score (0.0 to 1.0)
     */
    public double compareStrings(String str1, String str2) {
        return compareStringsEnhanced(str1, str2, 0.75);
    }

    /**
     * Enhanced string comparison with dynamic threshold and multiple algorithms
     * @param str1 First string
     * @param str2 Second string
     * @param baseThreshold Base threshold for dynamic adjustment
     * @return Enhanced similarity score (0.0 to 1.0)
     */
    public double compareStringsEnhanced(String str1, String str2, double baseThreshold) {
        if (StringUtils.isEmpty(str1) && StringUtils.isEmpty(str2)) {
            return 1.0;
        }
        if (StringUtils.isEmpty(str1) || StringUtils.isEmpty(str2)) {
            return 0.0;
        }

        // Exact match
        if (str1.equals(str2)) {
            return 1.0;
        }

        // Case-insensitive exact match
        if (str1.equalsIgnoreCase(str2)) {
            return 0.95;
        }

        // Use enhanced string matcher
        double enhancedScore = enhancedStringMatcher.calculateEnhancedSimilarity(str1, str2);
        
        // Fallback to traditional algorithms for comparison
        double levenshteinSimilarity = levenshteinDistance.calculateNormalizedSimilarity(str1, str2);
        double lcsSimilarity = longestCommonSubsequence.calculateNormalizedSimilarity(str1, str2);
        double traditionalScore = (levenshteinSimilarity * 0.6) + (lcsSimilarity * 0.4);

        // Return the better of enhanced or traditional score
        return Math.max(enhancedScore, traditionalScore);
    }

    /**
     * Special comparison for XPath strings with structural awareness
     * @param xpath1 First XPath
     * @param xpath2 Second XPath
     * @return Similarity score (0.0 to 1.0)
     */
    public double compareXPaths(String xpath1, String xpath2) {
        return compareXPathsEnhanced(xpath1, xpath2, 0.75);
    }

    /**
     * Enhanced XPath comparison with improved attribute matching
     * @param xpath1 First XPath
     * @param xpath2 Second XPath
     * @param baseThreshold Base threshold for dynamic adjustment
     * @return Enhanced similarity score (0.0 to 1.0)
     */
    public double compareXPathsEnhanced(String xpath1, String xpath2, double baseThreshold) {
        if (StringUtils.isEmpty(xpath1) && StringUtils.isEmpty(xpath2)) {
            return 1.0;
        }
        if (StringUtils.isEmpty(xpath1) || StringUtils.isEmpty(xpath2)) {
            return 0.0;
        }

        // Exact match
        if (xpath1.equals(xpath2)) {
            return 1.0;
        }

        // Extract element types and attributes from XPath
        String elementType1 = extractElementTypeFromXPath(xpath1);
        String elementType2 = extractElementTypeFromXPath(xpath2);
        
        String attribute1 = extractAttributeFromXPath(xpath1);
        String attribute2 = extractAttributeFromXPath(xpath2);

        // Enhanced element type comparison
        double elementTypeSimilarity = compareStringsEnhanced(elementType1, elementType2, baseThreshold);
        
        // Enhanced attribute comparison (most important for XPath)
        double attributeSimilarity = compareStringsEnhanced(attribute1, attribute2, baseThreshold);

        // Weight: attribute is most important, then element type
        double structuralSimilarity = (attributeSimilarity * 0.75) + (elementTypeSimilarity * 0.25);

        // Enhanced full XPath comparison
        double fullXPathSimilarity = compareStringsEnhanced(xpath1, xpath2, baseThreshold);

        // Return weighted combination favoring structural analysis
        return Math.max(structuralSimilarity, fullXPathSimilarity * 0.8);
    }

    /**
     * Extract element type from XPath (e.g., XCUIElementTypeButton)
     * @param xpath XPath string
     * @return Element type or empty string
     */
    private String extractElementTypeFromXPath(String xpath) {
        if (StringUtils.isEmpty(xpath)) {
            return "";
        }
        
        // Pattern: //XCUIElementTypeButton[@name='...']
        int startIndex = xpath.indexOf("XCUIElementType");
        if (startIndex == -1) {
            return "";
        }
        
        int endIndex = xpath.indexOf("[", startIndex);
        if (endIndex == -1) {
            endIndex = xpath.length();
        }
        
        return xpath.substring(startIndex, endIndex);
    }

    /**
     * Extract attribute value from XPath (e.g., name='loginButton')
     * @param xpath XPath string
     * @return Attribute value or empty string
     */
    private String extractAttributeFromXPath(String xpath) {
        if (StringUtils.isEmpty(xpath)) {
            return "";
        }
        
        // Pattern: [@name='loginButton']
        int nameStart = xpath.indexOf("@name='");
        if (nameStart != -1) {
            nameStart += 7; // Skip "@name='"
            int nameEnd = xpath.indexOf("'", nameStart);
            if (nameEnd != -1) {
                return xpath.substring(nameStart, nameEnd);
            }
        }
        
        return "";
    }

    /**
     * Find the best matching elements from a list based on similarity threshold
     * @param targetElement Element to match
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findBestMatches(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        return findBestMatchesEnhanced(targetElement, candidates, threshold, true);
    }

    /**
     * Enhanced best match finding with dynamic threshold and multi-level fallback
     * @param targetElement Element to match
     * @param candidates List of candidate elements
     * @param baseThreshold Base similarity threshold
     * @param useDynamicThreshold Whether to use dynamic threshold adjustment
     * @return List of matching elements sorted by similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findBestMatchesEnhanced(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double baseThreshold,
            boolean useDynamicThreshold) {
        
        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            // Skip self-comparison
            if (targetElement.getElementId() != null && 
                targetElement.getElementId().equals(candidate.getElementId())) {
                continue;
            }
            
            double similarity = compareElementsWithThreshold(targetElement, candidate, baseThreshold);
            
            // Calculate dynamic threshold for this specific comparison
            double effectiveThreshold = baseThreshold;
            if (useDynamicThreshold) {
                effectiveThreshold = calculateDynamicElementThreshold(targetElement, candidate, baseThreshold);
            }
            
            // Include matches above effective threshold OR top candidates with decent scores
            // VERY aggressive inclusion for near-misses - if similarity > 0.1, include it
            if (similarity >= effectiveThreshold || similarity >= 0.1) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        // Sort by similarity in descending order
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        return matches;
    }

    /**
     * Calculate dynamic threshold for element comparison based on element characteristics
     */
    private double calculateDynamicElementThreshold(TestElement target, TestElement candidate, double baseThreshold) {
        // Get key strings for threshold calculation
        String targetKey = getKeyIdentifier(target);
        String candidateKey = getKeyIdentifier(candidate);
        
        return enhancedStringMatcher.calculateDynamicThreshold(targetKey, candidateKey, baseThreshold);
    }

    /**
     * Get key identifier from element (prioritize accessibility_id, then name)
     */
    private String getKeyIdentifier(TestElement element) {
        if (!StringUtils.isEmpty(element.getAccessibilityId())) {
            return element.getAccessibilityId();
        }
        if (!StringUtils.isEmpty(element.getName())) {
            return element.getName();
        }
        if (!StringUtils.isEmpty(element.getElementId())) {
            return element.getElementId();
        }
        return "";
    }

    /**
     * Calculate context bonus based on element relationships
     */
    private double calculateContextBonus(TestElement element1, TestElement element2) {
        double bonus = 0.0;
        
        // Same screen bonus
        if (!StringUtils.isEmpty(element1.getScreen()) && 
            !StringUtils.isEmpty(element2.getScreen()) &&
            element1.getScreen().equalsIgnoreCase(element2.getScreen())) {
            bonus += 0.05;
        }
        
        // Same element type bonus
        if (!StringUtils.isEmpty(element1.getElementType()) && 
            !StringUtils.isEmpty(element2.getElementType()) &&
            element1.getElementType().equalsIgnoreCase(element2.getElementType())) {
            bonus += 0.03;
        }
        
        // Same class name bonus
        if (!StringUtils.isEmpty(element1.getClassName()) && 
            !StringUtils.isEmpty(element2.getClassName()) &&
            element1.getClassName().equalsIgnoreCase(element2.getClassName())) {
            bonus += 0.02;
        }
        
        return Math.min(bonus, 0.1); // Cap context bonus
    }

    /**
     * Check if two elements are exact matches based on available critical attributes
     * Only compares fields that are present in both elements
     * @param element1 First element
     * @param element2 Second element
     * @return true if exact match on all available critical attributes
     */
    public boolean isExactMatch(TestElement element1, TestElement element2) {
        if (element1 == null || element2 == null) {
            return false;
        }

        boolean hasAnyMatch = false;

        // Check accessibility_id if both have it
        if (hasValue(element1.getAccessibilityId()) && hasValue(element2.getAccessibilityId())) {
            if (!Objects.equals(element1.getAccessibilityId(), element2.getAccessibilityId())) {
                return false;
            }
            hasAnyMatch = true;
        }

        // Check name if both have it
        if (hasValue(element1.getName()) && hasValue(element2.getName())) {
            if (!Objects.equals(element1.getName(), element2.getName())) {
                return false;
            }
            hasAnyMatch = true;
        }

        // Check xpath if both have it
        if (hasValue(element1.getXpath()) && hasValue(element2.getXpath())) {
            if (!Objects.equals(element1.getXpath(), element2.getXpath())) {
                return false;
            }
            hasAnyMatch = true;
        }

        // Check element_id if both have it
        if (hasValue(element1.getElementId()) && hasValue(element2.getElementId())) {
            if (!Objects.equals(element1.getElementId(), element2.getElementId())) {
                return false;
            }
            hasAnyMatch = true;
        }

        // Return true only if at least one critical attribute was compared and all matched
        return hasAnyMatch;
    }

    /**
     * Compare elements based only on xpath
     * @param targetElement Element to search for
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by xpath similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findMatchesByXpath(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        if (!hasValue(targetElement.getXpath())) {
            return Collections.emptyList();
        }

        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            if (!hasValue(candidate.getXpath())) {
                continue;
            }
            
            // Use enhanced string comparison with dynamic threshold
            double similarity = compareStringsEnhanced(targetElement.getXpath(), candidate.getXpath(), threshold);
            
            // Calculate dynamic threshold for this specific comparison
            double dynamicThreshold = enhancedStringMatcher.calculateDynamicThreshold(
                targetElement.getXpath(), 
                candidate.getXpath(), 
                threshold
            );
            
            // Use the lower of the two thresholds for better near-miss detection
            double effectiveThreshold = Math.min(Math.min(threshold, dynamicThreshold), 0.2);
            
            if (similarity >= effectiveThreshold) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return matches;
    }

    /**
     * Compare elements based only on accessibility_id
     * @param targetElement Element to search for
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by accessibility_id similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findMatchesByAccessibilityId(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        if (!hasValue(targetElement.getAccessibilityId())) {
            return Collections.emptyList();
        }

        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            if (!hasValue(candidate.getAccessibilityId())) {
                continue;
            }
            
            // Use enhanced string comparison with dynamic threshold
            double similarity = compareStringsEnhanced(
                targetElement.getAccessibilityId(), 
                candidate.getAccessibilityId(), 
                threshold
            );
            
            // Calculate dynamic threshold for this specific comparison
            double dynamicThreshold = enhancedStringMatcher.calculateDynamicThreshold(
                targetElement.getAccessibilityId(), 
                candidate.getAccessibilityId(), 
                threshold
            );
            
            // Use the lower of the two thresholds for better near-miss detection
            double effectiveThreshold = Math.min(Math.min(threshold, dynamicThreshold), 0.2);
            
            if (similarity >= effectiveThreshold) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return matches;
    }

    /**
     * Compare elements based only on element_id
     * @param targetElement Element to search for
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by element_id similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findMatchesByElementId(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        if (!hasValue(targetElement.getElementId())) {
            return Collections.emptyList();
        }

        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            if (!hasValue(candidate.getElementId())) {
                continue;
            }
            
            // Use enhanced string comparison with dynamic threshold
            double similarity = compareStringsEnhanced(
                targetElement.getElementId(), 
                candidate.getElementId(), 
                threshold
            );
            
            // Calculate dynamic threshold for this specific comparison
            double dynamicThreshold = enhancedStringMatcher.calculateDynamicThreshold(
                targetElement.getElementId(), 
                candidate.getElementId(), 
                threshold
            );
            
            // Use the lower of the two thresholds for better near-miss detection (especially important for element_id)
            double effectiveThreshold = Math.min(Math.min(threshold, dynamicThreshold), 0.15);
            
            if (similarity >= effectiveThreshold) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return matches;
    }

    /**
     * Compare elements based only on class_name
     * @param targetElement Element to search for
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by class_name similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findMatchesByClassName(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        if (!hasValue(targetElement.getClassName())) {
            return Collections.emptyList();
        }

        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            if (!hasValue(candidate.getClassName())) {
                continue;
            }
            
            // Use enhanced string comparison with dynamic threshold
            double similarity = compareStringsEnhanced(
                targetElement.getClassName(), 
                candidate.getClassName(), 
                threshold
            );
            
            // Calculate dynamic threshold for this specific comparison
            double dynamicThreshold = enhancedStringMatcher.calculateDynamicThreshold(
                targetElement.getClassName(), 
                candidate.getClassName(), 
                threshold
            );
            
            // Use the lower of the two thresholds for better near-miss detection
            double effectiveThreshold = Math.min(Math.min(threshold, dynamicThreshold), 0.2);
            
            if (similarity >= effectiveThreshold) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return matches;
    }

    /**
     * Compare elements based only on name
     * @param targetElement Element to search for
     * @param candidates List of candidate elements
     * @param threshold Minimum similarity threshold
     * @return List of matching elements sorted by name similarity (descending)
     */
    public List<Map.Entry<TestElement, Double>> findMatchesByName(
            TestElement targetElement, 
            List<TestElement> candidates, 
            double threshold) {
        
        if (!hasValue(targetElement.getName())) {
            return Collections.emptyList();
        }

        List<Map.Entry<TestElement, Double>> matches = new ArrayList<>();
        
        for (TestElement candidate : candidates) {
            if (!hasValue(candidate.getName())) {
                continue;
            }
            
            // Use enhanced string comparison with dynamic threshold
            double similarity = compareStringsEnhanced(
                targetElement.getName(), 
                candidate.getName(), 
                threshold
            );
            
            // Calculate dynamic threshold for this specific comparison
            double dynamicThreshold = enhancedStringMatcher.calculateDynamicThreshold(
                targetElement.getName(), 
                candidate.getName(), 
                threshold
            );
            
            // Use the lower of the two thresholds for better near-miss detection
            double effectiveThreshold = Math.min(Math.min(threshold, dynamicThreshold), 0.2);
            
            if (similarity >= effectiveThreshold) {
                matches.add(new AbstractMap.SimpleEntry<>(candidate, similarity));
            }
        }
        
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return matches;
    }
} 