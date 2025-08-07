package com.selfhealing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestElement {
    
    @JsonProperty(value = "element_id", required = false)
    private String elementId;
    
    @JsonProperty(value = "xpath", required = false)
    private String xpath;
    
    @JsonProperty(value = "accessibility_id", required = false)
    private String accessibilityId;
    
    @JsonProperty(value = "class_name", required = false)
    private String className;
    
    @JsonProperty(value = "name", required = false)
    private String name;
    
    @JsonProperty(value = "screen", required = false)
    private String screen;
    
    @JsonProperty(value = "element_type", required = false)
    private String elementType;

    // Default constructor
    public TestElement() {}
    
    // Constructor with all fields
    public TestElement(String elementId, String xpath, String accessibilityId, 
                      String className, String name, String screen, String elementType) {
        this.elementId = elementId;
        this.xpath = xpath;
        this.accessibilityId = accessibilityId;
        this.className = className;
        this.name = name;
        this.screen = screen;
        this.elementType = elementType;
    }

    // Getters and Setters
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getAccessibilityId() {
        return accessibilityId;
    }

    public void setAccessibilityId(String accessibilityId) {
        this.accessibilityId = accessibilityId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return "TestElement{" +
                "elementId='" + elementId + '\'' +
                ", xpath='" + xpath + '\'' +
                ", accessibilityId='" + accessibilityId + '\'' +
                ", className='" + className + '\'' +
                ", name='" + name + '\'' +
                ", screen='" + screen + '\'' +
                ", elementType='" + elementType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TestElement that = (TestElement) obj;
        return elementId != null ? elementId.equals(that.elementId) : that.elementId == null;
    }

    @Override
    public int hashCode() {
        return elementId != null ? elementId.hashCode() : 0;
    }
} 