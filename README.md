# Self-Healing LSC iOS

A Spring Boot application that provides self-healing capabilities for iOS test automation using advanced string similarity algorithms including Levenshtein Distance and Longest Common Subsequence (LCS).

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Algorithm Details](#algorithm-details)
- [Contributing](#contributing)

## 🔍 Overview

Self-Healing LSC iOS is designed to solve the common problem of brittle test automation scripts that break when UI elements change. The application uses sophisticated string matching algorithms to automatically find similar elements when exact matches fail, enabling test scripts to self-heal and continue execution.

### Key Use Cases

- **Test Automation Resilience**: Automatically adapt to minor UI changes
- **Element Locator Self-Healing**: Find alternative locators when primary ones fail
- **Test Maintenance Reduction**: Minimize manual intervention in test script updates
- **iOS App Testing**: Specialized for iOS XCUITest element identification

## ✨ Features

### Core Functionality
- **Advanced String Matching**: Enhanced Levenshtein Distance and LCS algorithms
- **Dynamic Threshold Calculation**: Adaptive similarity thresholds based on string characteristics
- **Multi-Strategy Element Finding**: Multiple fallback strategies for element location
- **Element Validation**: Comprehensive validation of test element structures
- **Auto-Update Capability**: Automatic updating of element definitions when better matches are found

### Specialized Algorithms
- **Enhanced String Matcher**: Custom algorithm with iOS-specific optimizations
- **Abbreviation Support**: Recognition of common iOS element abbreviation patterns  
- **Partial Matching**: Intelligent handling of substring and prefix/suffix matches
- **Length-Aware Scoring**: Adaptive scoring based on string length characteristics

### API Features
- **RESTful API**: Clean, well-documented REST endpoints
- **Multiple Search Strategies**: Search by ID, XPath, accessibility ID, class name, or name
- **Suggestion Engine**: Get multiple alternative matches ranked by similarity
- **Statistics and Monitoring**: Built-in metrics and health checks
- **Configuration Management**: Runtime configuration of similarity thresholds

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│              REST API Layer              │
├─────────────────────────────────────────┤
│   • SelfHealingController               │
│   • TestController                      │
│   • DebugController                     │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│            Service Layer                │
├─────────────────────────────────────────┤
│   • SelfHealingService                  │
│   • ElementComparisonService            │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│           Algorithm Layer               │
├─────────────────────────────────────────┤
│   • EnhancedStringMatcher               │
│   • LevenshteinDistance                 │
│   • LongestCommonSubsequence            │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│             Data Layer                  │
├─────────────────────────────────────────┤
│   • elements.json                       │
│   • TestElement Model                   │
│   • SimilarityResult Model              │
└─────────────────────────────────────────┘
```

### Component Description

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and orchestration
- **Algorithms**: Core string matching and similarity calculations
- **Models**: Data structures for elements and results
- **Configuration**: Spring Boot configuration and properties

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd selfhealing-lsc-ios
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify installation**
   ```bash
   curl http://localhost:8080/api/v1/self-healing/health
   ```

### Quick Start

Once the application is running, you can immediately test it:

```bash
# Get a sample element
curl http://localhost:8080/api/v1/test/sample-element

# Test element finding
curl -X POST http://localhost:8080/api/v1/self-healing/find \
  -H "Content-Type: application/json" \
  -d '{
    "element_id": "login_submit_button",
    "xpath": "//XCUIElementTypeButton[@name='\''loginButton'\'']",
    "accessibility_id": "loginButton"
  }'
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Core Endpoints

#### Health Check
- `GET /api/v1/self-healing/health` - Service health status
- `GET /actuator/health` - Spring Boot actuator health

#### Configuration
- `GET /api/v1/self-healing/config` - Get current configuration
- `GET /api/v1/self-healing/stats` - Get system statistics

#### Element Operations
- `POST /api/v1/self-healing/find` - Find element with self-healing
- `POST /api/v1/self-healing/suggestions` - Get element suggestions
- `POST /api/v1/self-healing/validate` - Validate element structure
- `PUT /api/v1/self-healing/update/{id}` - Update element definition

#### Specialized Finding
- `POST /api/v1/self-healing/find-by-xpath` - Find by XPath only
- `POST /api/v1/self-healing/find-by-accessibility-id` - Find by accessibility ID
- `POST /api/v1/self-healing/find-by-element-id` - Find by element ID
- `POST /api/v1/self-healing/find-by-class-name` - Find by class name
- `POST /api/v1/self-healing/find-by-name` - Find by name

#### Testing & Debug
- `POST /api/v1/test/string-similarity` - Test string similarity
- `POST /api/v1/test/element-similarity` - Test element similarity
- `POST /api/v1/test/xpath-similarity` - Test XPath similarity
- `POST /api/v1/debug/string-similarity` - Debug enhanced similarity
- `GET /api/v1/test/sample-element` - Get sample test data

### Request/Response Examples

#### Find Element Request
```json
{
  "element_id": "login_submit_button",
  "xpath": "//XCUIElementTypeButton[@name='loginButton']",
  "accessibility_id": "loginButton",
  "class_name": "XCUIElementTypeButton",
  "name": "loginButton",
  "screen": "LoginScreen",
  "element_type": "button"
}
```

#### Find Element Response
```json
{
  "success": true,
  "result": {
    "originalElement": { ... },
    "matchedElement": { ... },
    "similarityScore": 0.95,
    "matchType": "SIMILARITY",
    "autoUpdated": true
  },
  "message": "Element found - similarity match (95.00%)"
}
```

## ⚙️ Configuration

### Application Properties

```properties
# Server Configuration
server.port=8080
spring.application.name=self-healing-lsc-ios

# Self-healing Configuration
selfhealing.similarity.threshold=0.75
selfhealing.auto-update.enabled=true
selfhealing.max-suggestions=5

# Logging
logging.level.com.selfhealing=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Key Configuration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `selfhealing.similarity.threshold` | `0.75` | Minimum similarity score for matches |
| `selfhealing.auto-update.enabled` | `true` | Enable automatic element updates |
| `selfhealing.max-suggestions` | `5` | Maximum number of suggestions to return |

## 💡 Usage Examples

### Basic Element Finding

```java
// Test element with a typo in element_id
TestElement brokenElement = new TestElement();
brokenElement.setElementId("login_submit_butto"); // Missing 'n'
brokenElement.setXpath("//XCUIElementTypeButton[@name='loginButton']");
brokenElement.setAccessibilityId("loginButton");

// The service will find the correct element despite the typo
SimilarityResult result = selfHealingService.findElement(brokenElement);
```

### Getting Suggestions

```java
// Get multiple suggestions for an element
List<SimilarityResult> suggestions = selfHealingService.getSuggestions(targetElement);

for (SimilarityResult suggestion : suggestions) {
    System.out.println("Match: " + suggestion.getMatchedElement().getElementId() + 
                      " (Score: " + suggestion.getSimilarityScore() + ")");
}
```

### String Similarity Testing

```java
// Test string similarity algorithms
double similarity = enhancedStringMatcher.calculateEnhancedSimilarity(
    "login_submit_button", 
    "login_submit_butto"
);
// Returns ~0.95 (95% similarity)
```

### Custom Threshold Calculation

```java
// Dynamic threshold based on string characteristics
double threshold = enhancedStringMatcher.calculateDynamicThreshold(
    "login_button", 
    "login_btn", 
    0.75
);
// Returns lower threshold for abbreviation-like matches
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SelfHealingServiceTest

# Run with coverage
mvn test jacoco:report
```

### Postman Collection

Import the included `Self-Healing-LSC-iOS.postman_collection.json` for comprehensive API testing:

1. **Health Checks**: Verify service availability
2. **Element Finding**: Test various finding strategies
3. **Algorithm Testing**: Test string similarity algorithms
4. **Error Cases**: Test error handling and edge cases
5. **Performance Tests**: Load testing scenarios

### Test Data

The application includes comprehensive test data in `src/main/resources/elements.json` with over 450 iOS UI elements across multiple screens:

- **WelcomeScreen**: Logo, buttons, language selector
- **LoginScreen**: Email/password fields, social login buttons
- **HomeScreen**: Search bar, carousels, product items
- **CartScreen**: Item management, checkout flow
- **ProfileScreen**: User management, settings
- And many more...

## 🔬 Algorithm Details

### Enhanced String Matcher

Our custom algorithm combines multiple techniques:

1. **Base Similarity Calculation**
   - Levenshtein Distance (30% weight)
   - LCS (20% weight)
   - Substring matching (50% weight)

2. **Bonus Calculations**
   - Abbreviation recognition
   - Prefix/suffix matching
   - Length adjustments
   - iOS-specific patterns

3. **Dynamic Threshold Adjustment**
   - Very similar strings (1-3 char diff): 15% threshold
   - Containment matches: 20% threshold
   - Prefix matches: 25% threshold
   - Length-based adjustments

### Levenshtein Distance

Classic edit distance algorithm optimized for:
- Case-insensitive matching
- Normalized similarity scoring
- Performance optimizations for short strings

### LCS (Longest Common Subsequence)

Finds the longest subsequence common to both strings:
- Preserves character order
- Good for detecting partial matches
- Complements Levenshtein for comprehensive analysis

### Similarity Scoring Strategy

```
Enhanced Score = (Levenshtein × 0.3) + (LCS × 0.2) + (Substring × 0.5) 
                 + Abbreviation Bonus + Length Adjustment
```

## 🧩 Element Model

### TestElement Structure

```json
{
  "element_id": "string",         // Primary identifier
  "xpath": "string",              // XPath locator
  "accessibility_id": "string",   // iOS accessibility identifier
  "class_name": "string",         // UI element class
  "name": "string",               // Element name attribute
  "screen": "string",             // Screen/page context
  "element_type": "string"        // Element type (button, textfield, etc.)
}
```

### SimilarityResult Structure

```json
{
  "originalElement": { ... },      // Input element
  "matchedElement": { ... },       // Best match found
  "similarityScore": 0.95,         // Similarity score (0.0-1.0)
  "matchType": "SIMILARITY",       // Match type classification
  "autoUpdated": true              // Whether auto-update occurred
}
```

## 🚀 Performance Considerations

### Optimization Strategies

1. **Caching**: Element data cached for 1 minute
2. **Early Termination**: Stop processing when exact match found
3. **Dynamic Thresholds**: Reduce unnecessary calculations
4. **String Preprocessing**: Normalize strings once

### Scalability

- **Memory**: Efficient string algorithms with O(n×m) complexity
- **CPU**: Optimized for typical element ID lengths (5-50 characters)
- **Throughput**: Handles 100+ requests per second on standard hardware

## 🤝 Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc for public methods
- Keep methods focused and small
- Write comprehensive tests

### Reporting Issues

Please use the GitHub issue tracker to report bugs or request features. Include:

- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, OS, etc.)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

- Check the [API Documentation](#api-documentation)
- Review [Usage Examples](#usage-examples)
- Test with the [Postman Collection](#testing)
- Create an issue on GitHub

## 🏷️ Version History

### v1.0.0 (Current)
- Initial release
- Enhanced string matching algorithms
- Comprehensive REST API
- iOS-specific optimizations
- Dynamic threshold calculation
- Auto-update capabilities

---

**Built with ❤️ for iOS test automation engineers who want their tests to heal themselves!**
