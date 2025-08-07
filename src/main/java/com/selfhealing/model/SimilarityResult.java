package com.selfhealing.model;

public class SimilarityResult {
    
    private TestElement originalElement;
    private TestElement matchedElement;
    private double similarityScore;
    private String matchType; // "EXACT", "SIMILARITY", "NOT_FOUND"
    private boolean autoUpdated;
    
    public SimilarityResult() {}
    
    public SimilarityResult(TestElement originalElement, TestElement matchedElement, 
                           double similarityScore, String matchType, boolean autoUpdated) {
        this.originalElement = originalElement;
        this.matchedElement = matchedElement;
        this.similarityScore = similarityScore;
        this.matchType = matchType;
        this.autoUpdated = autoUpdated;
    }

    // Getters and Setters
    public TestElement getOriginalElement() {
        return originalElement;
    }

    public void setOriginalElement(TestElement originalElement) {
        this.originalElement = originalElement;
    }

    public TestElement getMatchedElement() {
        return matchedElement;
    }

    public void setMatchedElement(TestElement matchedElement) {
        this.matchedElement = matchedElement;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public boolean isAutoUpdated() {
        return autoUpdated;
    }

    public void setAutoUpdated(boolean autoUpdated) {
        this.autoUpdated = autoUpdated;
    }

    @Override
    public String toString() {
        return "SimilarityResult{" +
                "originalElement=" + originalElement +
                ", matchedElement=" + matchedElement +
                ", similarityScore=" + similarityScore +
                ", matchType='" + matchType + '\'' +
                ", autoUpdated=" + autoUpdated +
                '}';
    }
} 