package com.selfhealing.algorithm;

import org.springframework.stereotype.Component;

/**
 * Longest Common Subsequence (LCS) Algorithm Implementation
 * Finds the longest subsequence common to two sequences.
 */
@Component
public class LongestCommonSubsequence {

    /**
     * Calculate the length of the longest common subsequence
     * @param str1 First string
     * @param str2 Second string
     * @return Length of LCS
     */
    public int calculateLCSLength(String str1, String str2) {
        if (str1 == null || str2 == null) return 0;
        
        int len1 = str1.length();
        int len2 = str2.length();
        
        // Create a matrix to store LCS lengths
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // Fill the matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * Get the actual longest common subsequence string
     * @param str1 First string
     * @param str2 Second string
     * @return LCS string
     */
    public String getLCS(String str1, String str2) {
        if (str1 == null || str2 == null) return "";
        
        int len1 = str1.length();
        int len2 = str2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // Build the LCS length matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        // Backtrack to find the LCS string
        StringBuilder lcs = new StringBuilder();
        int i = len1, j = len2;
        
        while (i > 0 && j > 0) {
            if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                lcs.insert(0, str1.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        
        return lcs.toString();
    }
    
    /**
     * Calculate similarity based on LCS
     * @param str1 First string
     * @param str2 Second string
     * @return Similarity percentage (0.0 to 1.0)
     */
    public double calculateSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) return 1.0;
        if (str1 == null || str2 == null) return 0.0;
        
        int lcsLength = calculateLCSLength(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        
        if (maxLength == 0) return 1.0;
        
        return (double) lcsLength / maxLength;
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
    
    /**
     * Calculate combined similarity using both LCS and relative positioning
     * This method considers the order and proximity of matching characters
     * @param str1 First string
     * @param str2 Second string
     * @return Enhanced similarity percentage (0.0 to 1.0)
     */
    public double calculateEnhancedSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) return 1.0;
        if (str1 == null || str2 == null) return 0.0;
        
        double lcsSimilarity = calculateSimilarity(str1, str2);
        
        // Calculate character frequency similarity
        double frequencySimilarity = calculateCharacterFrequencySimilarity(str1, str2);
        
        // Weighted combination of LCS and frequency similarity
        return (lcsSimilarity * 0.7) + (frequencySimilarity * 0.3);
    }
    
    /**
     * Calculate similarity based on character frequencies
     * @param str1 First string
     * @param str2 Second string
     * @return Frequency-based similarity (0.0 to 1.0)
     */
    private double calculateCharacterFrequencySimilarity(String str1, String str2) {
        if (str1.isEmpty() && str2.isEmpty()) return 1.0;
        
        // Count character frequencies
        int[] freq1 = new int[256];
        int[] freq2 = new int[256];
        
        for (char c : str1.toCharArray()) {
            freq1[c]++;
        }
        
        for (char c : str2.toCharArray()) {
            freq2[c]++;
        }
        
        // Calculate similarity based on frequency differences
        int commonCharacters = 0;
        int totalCharacters = 0;
        
        for (int i = 0; i < 256; i++) {
            commonCharacters += Math.min(freq1[i], freq2[i]);
            totalCharacters += Math.max(freq1[i], freq2[i]);
        }
        
        return totalCharacters == 0 ? 1.0 : (double) commonCharacters / totalCharacters;
    }
} 