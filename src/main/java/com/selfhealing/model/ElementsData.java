package com.selfhealing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ElementsData {
    
    @JsonProperty("test_elements")
    private List<TestElement> testElements;

    public ElementsData() {}

    public ElementsData(List<TestElement> testElements) {
        this.testElements = testElements;
    }

    public List<TestElement> getTestElements() {
        return testElements;
    }

    public void setTestElements(List<TestElement> testElements) {
        this.testElements = testElements;
    }

    @Override
    public String toString() {
        return "ElementsData{" +
                "testElements=" + (testElements != null ? testElements.size() : 0) + " elements" +
                '}';
    }
} 