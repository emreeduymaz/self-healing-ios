package com.selfhealing.algorithm;

import org.springframework.stereotype.Component;

/**
 * Levenshtein Distance Algorithm Implementation
 * Calculates the minimum number of single-character edits (insertions, deletions, substitutions)
 * required to change one string into another.
 */
@Component
public class LevenshteinDistance {

    /**
     * Calculate Levenshtein distance between two strings
     * @param str1 First string
     * @param str2 Second string
     * @return Levenshtein distance
     */
    public int calculateDistance(String str1, String str2) {
        if (str1 == null && str2 == null) return 0;
        if (str1 == null) return str2.length();
        if (str2 == null) return str1.length();
        
        int len1 = str1.length();
        int len2 = str2.length();
        
        // Create a matrix to store the distances
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // Initialize first row and column
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // Fill the matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // No operation needed
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j],     // Deletion
                                dp[i][j - 1]),     // Insertion
                        dp[i - 1][j - 1]          // Substitution
                    );
                }
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * Calculate similarity percentage based on Levenshtein distance
     * @param str1 First string
     * @param str2 Second string
     * @return Similarity percentage (0.0 to 1.0)
     */
    public double calculateSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) return 1.0;
        if (str1 == null || str2 == null) return 0.0;
        
        int distance = calculateDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        
        if (maxLength == 0) return 1.0;
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Calculate normalized similarity (case-insensitive)
     * @param str1 First string
     * @param str2 Second string
     * @return Normalized similarity percentage (0.0 to 1.0)
     */
    public double calculateNormalizedSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) return 1.0;
        if (str1 == null || str2 == null) return 0.0;
        
        String normalizedStr1 = str1.toLowerCase().trim();
        String normalizedStr2 = str2.toLowerCase().trim();
        
        return calculateSimilarity(normalizedStr1, normalizedStr2);
    }
} 