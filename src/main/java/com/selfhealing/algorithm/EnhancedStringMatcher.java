package com.selfhealing.algorithm;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Enhanced String Matcher with abbreviation support, partial matching, and dynamic scoring
 */
@Component
public class EnhancedStringMatcher {

    // Common abbreviation patterns for iOS elements
    private static final Map<String, List<String>> ABBREVIATION_PATTERNS = new HashMap<>();

    /**
     * Enhanced similarity calculation with multiple fallback levels
     */
    public double calculateEnhancedSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) return 1.0;
        if (str1 == null || str2 == null) return 0.0;
        
        String normalized1 = str1.toLowerCase().trim();
        String normalized2 = str2.toLowerCase().trim();
        
        // Exact match
        if (normalized1.equals(normalized2)) return 1.0;
        
        // Calculate base similarities
        double levenshteinSim = calculateLevenshteinSimilarity(normalized1, normalized2);
        double lcsSim = calculateLCSSimilarity(normalized1, normalized2);
        double substringBonus = calculateSubstringBonus(normalized1, normalized2);
        double abbreviationBonus = calculateAbbreviationBonus(normalized1, normalized2);
        double lengthAdjustment = calculateLengthAdjustment(normalized1, normalized2);
        
        // Combine scores with weights - prioritize substring matching for near-misses
        double baseSimilarity = (levenshteinSim * 0.3) + (lcsSim * 0.2) + (substringBonus * 0.5);
        
        // Apply bonuses and adjustments
        double enhancedScore = baseSimilarity + abbreviationBonus + lengthAdjustment;
        
        return Math.max(0.0, Math.min(1.0, enhancedScore));
    }

    /**
     * Dynamic threshold calculation based on string characteristics
     * Much more aggressive for near-matches like "login_submit_butto" vs "login_submit_button"
     */
    public double calculateDynamicThreshold(String str1, String str2, double baseThreshold) {
        if (str1 == null || str2 == null) return baseThreshold;
        
        String normalized1 = str1.toLowerCase().trim();
        String normalized2 = str2.toLowerCase().trim();
        
        int minLength = Math.min(normalized1.length(), normalized2.length());
        int maxLength = Math.max(normalized1.length(), normalized2.length());
        int lengthDiff = maxLength - minLength;
        
        // For very similar strings (difference of 1-3 characters), use very low threshold
        if (lengthDiff <= 3 && minLength >= 10) {
            return 0.15; // Very low threshold for near-exact matches
        }
        
        // Check if one string is almost entirely contained in the other
        String shorter = normalized1.length() <= normalized2.length() ? normalized1 : normalized2;
        String longer = normalized1.length() > normalized2.length() ? normalized1 : normalized2;
        
        if (longer.contains(shorter) && shorter.length() >= 5) {
            return 0.20; // Low threshold for containment matches
        }
        
        // Check for missing/extra characters at the end (common typo)
        if (minLength >= 8) {
            if (longer.startsWith(shorter) || shorter.startsWith(longer)) {
                return 0.25; // Low threshold for prefix matches
            }
        }
        
        // Adjust threshold based on string lengths (more aggressive)
        if (minLength <= 3) {
            return Math.max(0.15, baseThreshold - 0.5); // Very short strings
        } else if (minLength <= 6) {
            return Math.max(0.25, baseThreshold - 0.4); // Short strings
        } else if (maxLength <= 15) {
            return Math.max(0.30, baseThreshold - 0.3); // Medium strings
        }
        
        return Math.max(0.35, baseThreshold - 0.2); // Normal threshold for longer strings
    }

    /**
     * Multi-level matching with fallback strategies
     */
    public MatchResult findBestMatch(String target, List<String> candidates, double baseThreshold) {
        MatchResult bestMatch = new MatchResult();
        bestMatch.score = 0.0;
        bestMatch.matchType = "NOT_FOUND";
        
        for (String candidate : candidates) {
            double similarity = calculateEnhancedSimilarity(target, candidate);
            double dynamicThreshold = calculateDynamicThreshold(target, candidate, baseThreshold);
            
            if (similarity > bestMatch.score) {
                bestMatch.score = similarity;
                bestMatch.matchedString = candidate;
                
                if (similarity >= baseThreshold) {
                    bestMatch.matchType = "HIGH_CONFIDENCE";
                } else if (similarity >= dynamicThreshold) {
                    bestMatch.matchType = "MEDIUM_CONFIDENCE";
                } else if (similarity >= 0.3) {
                    bestMatch.matchType = "LOW_CONFIDENCE";
                } else {
                    bestMatch.matchType = "PARTIAL_MATCH";
                }
            }
        }
        
        return bestMatch;
    }

    /**
     * Calculate substring bonus for partial matches
     * More aggressive bonuses for near-misses
     */
    private double calculateSubstringBonus(String str1, String str2) {
        double bonus = 0.0;
        
        String normalized1 = str1.toLowerCase().trim();
        String normalized2 = str2.toLowerCase().trim();
        
        // Check if shorter string is contained in longer string
        String shorter = normalized1.length() <= normalized2.length() ? normalized1 : normalized2;
        String longer = normalized1.length() > normalized2.length() ? normalized1 : normalized2;
        
        if (longer.contains(shorter)) {
            // Much higher bonus for containment
            double coverage = (double) shorter.length() / longer.length();
            bonus += coverage * 0.5; // Increased from 0.3
            
            // Extra bonus if the shorter string starts the longer string (prefix match)
            if (longer.startsWith(shorter)) {
                bonus += 0.3; // Increased from 0.2
            }
            
            // Extra bonus if the shorter string ends the longer string (suffix match)
            if (longer.endsWith(shorter)) {
                bonus += 0.25;
            }
        }
        
        // Check for very close matches (1-3 character difference)
        int lengthDiff = Math.abs(normalized1.length() - normalized2.length());
        if (lengthDiff <= 3 && Math.min(normalized1.length(), normalized2.length()) >= 8) {
            // Check how many characters match in sequence
            int maxCommonLength = calculateMaxCommonSubstring(normalized1, normalized2);
            double commonRatio = (double) maxCommonLength / Math.max(normalized1.length(), normalized2.length());
            
            if (commonRatio >= 0.75) { // 75% of characters match in sequence (lowered from 80%)
                bonus += 0.5; // Increased bonus from 0.4 to 0.5
            } else if (commonRatio >= 0.6) { // Additional bonus for 60%+ matches
                bonus += 0.3;
            }
        }
        
        // Additional bonus for very similar strings regardless of length
        int maxCommonLength = calculateMaxCommonSubstring(normalized1, normalized2);
        double commonRatio = (double) maxCommonLength / Math.max(normalized1.length(), normalized2.length());
        if (commonRatio >= 0.85) { // 85%+ common substring gets big bonus
            bonus += 0.4;
        }
        
        // Check for common subsequences
        bonus += calculateCommonSubsequenceBonus(normalized1, normalized2);
        
        return Math.min(bonus, 0.6); // Increased cap from 0.4 to 0.6
    }
    
    /**
     * Calculate the length of the longest common substring
     */
    private int calculateMaxCommonSubstring(String str1, String str2) {
        int maxLength = 0;
        
        for (int i = 0; i < str1.length(); i++) {
            for (int j = 0; j < str2.length(); j++) {
                int length = 0;
                while (i + length < str1.length() && 
                       j + length < str2.length() && 
                       str1.charAt(i + length) == str2.charAt(j + length)) {
                    length++;
                }
                maxLength = Math.max(maxLength, length);
            }
        }
        
        return maxLength;
    }

    /**
     * Calculate abbreviation bonus
     */
    private double calculateAbbreviationBonus(String str1, String str2) {
        double bonus = 0.0;
        
        // Check if either string is an abbreviation of common patterns
        bonus += checkAbbreviationMatch(str1, str2);
        bonus += checkAbbreviationMatch(str2, str1);
        
        // Check for common iOS element abbreviations
        bonus += checkiOSPatterns(str1, str2);
        
        return Math.min(bonus, 0.3); // Cap the bonus
    }

    /**
     * Calculate length adjustment for short strings
     */
    private double calculateLengthAdjustment(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        int maxLength = Math.max(str1.length(), str2.length());
        
        // Reduce penalty for very short strings
        if (minLength <= 3 && maxLength <= 8) {
            return 0.15; // Bonus for short string comparisons
        } else if (minLength <= 5 && maxLength <= 12) {
            return 0.1; // Small bonus for medium short strings
        }
        
        // Penalty for very different lengths
        double lengthRatio = (double) minLength / maxLength;
        if (lengthRatio < 0.3) {
            return -0.1; // Penalty for very different lengths
        }
        
        return 0.0;
    }

    /**
     * Basic Levenshtein similarity (simplified)
     */
    private double calculateLevenshteinSimilarity(String str1, String str2) {
        int distance = calculateLevenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }

    /**
     * Basic LCS similarity (simplified)
     */
    private double calculateLCSSimilarity(String str1, String str2) {
        int lcsLength = calculateLCSLength(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return maxLength == 0 ? 1.0 : (double) lcsLength / maxLength;
    }

    /**
     * Check abbreviation patterns
     */
    private double checkAbbreviationMatch(String abbr, String full) {
        List<String> expansions = ABBREVIATION_PATTERNS.get(abbr.toLowerCase());
        if (expansions != null) {
            for (String expansion : expansions) {
                if (full.toLowerCase().contains(expansion)) {
                    return 0.25; // Significant bonus for abbreviation match
                }
            }
        }
        return 0.0;
    }

    /**
     * Check common iOS element patterns
     */
    private double checkiOSPatterns(String str1, String str2) {
        double bonus = 0.0;
        
        // Common iOS element name patterns
        String[] patterns = {"button", "field", "text", "image", "label", "view", "screen"};
        
        for (String pattern : patterns) {
            if ((str1.contains(pattern) && str2.contains(pattern.substring(0, Math.min(3, pattern.length())))) ||
                (str2.contains(pattern) && str1.contains(pattern.substring(0, Math.min(3, pattern.length()))))) {
                bonus += 0.1;
            }
        }
        
        return Math.min(bonus, 0.2);
    }

    /**
     * Calculate common subsequence bonus
     */
    private double calculateCommonSubsequenceBonus(String str1, String str2) {
        Set<String> ngrams1 = generateNGrams(str1, 2);
        Set<String> ngrams2 = generateNGrams(str2, 2);
        
        Set<String> common = new HashSet<>(ngrams1);
        common.retainAll(ngrams2);
        
        if (ngrams1.isEmpty() || ngrams2.isEmpty()) return 0.0;
        
        double commonRatio = (double) common.size() / Math.max(ngrams1.size(), ngrams2.size());
        return commonRatio * 0.15;
    }

    /**
     * Generate n-grams for a string
     */
    private Set<String> generateNGrams(String str, int n) {
        Set<String> ngrams = new HashSet<>();
        if (str.length() < n) return ngrams;
        
        for (int i = 0; i <= str.length() - n; i++) {
            ngrams.add(str.substring(i, i + n));
        }
        return ngrams;
    }

    // Simple implementations for core algorithms
    private int calculateLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        for (int i = 0; i <= str1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= str2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i-1) == str2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]);
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }

    private int calculateLCSLength(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i-1) == str2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }

    /**
     * Result class for matching operations
     */
    public static class MatchResult {
        public double score;
        public String matchedString;
        public String matchType;
        
        public MatchResult() {
            this.score = 0.0;
            this.matchType = "NOT_FOUND";
        }
    }
} 