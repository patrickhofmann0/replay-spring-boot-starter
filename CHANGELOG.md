# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Code quality plugins: JaCoCo for test coverage, Checkstyle for code style, SpotBugs for bug detection
- Content-Type filtering configuration (`replay.includeContentTypes`) to control which response types to capture
- Dashboard path configuration (`replay.dashboardPath`) to customize the dashboard URL path
- Input validation for Dashboard Controller endpoints
- @Validated annotations to ReplayProperties with @Min and @NotNull constraints
- Release profile with GPG signing plugin for Maven Central deployment
- Distribution management configuration for OSSRH/Maven Central
- Response header capture and redaction for security-sensitive headers
- HashMap index for O(1) request lookup performance in InMemoryReplayRequestStorage
- TestDataFactory helper class with builder pattern for easier test data creation

### Changed
- **BREAKING**: CapturedRequest record signature changed from 9 to 11 parameters:
  - Added: `responseHeaders` (Map<String, String>)
  - Added: `responseContentType` (String)
  - Renamed: `headers` → `requestHeaders`
- Replaced LinkedList with ArrayDeque for better performance in InMemoryReplayRequestStorage
- Made `findById()` method synchronized for thread-safety
- Improved exception handling with more specific catch blocks
- Fixed toLowerCase() to use Locale.ROOT to avoid locale-dependent issues

### Fixed
- **BLOCKER**: AutoConfiguration.imports had wrong package name - starter didn't load at all
- **BLOCKER**: POM dependencies had wrong scope (`provided`) - Thymeleaf was missing from JAR
- **SECURITY**: Response headers (Set-Cookie, etc.) were not redacted - session hijacking risk
- Removed wildcard imports and replaced with explicit imports
- Fixed GitHub repository link in README.md
- Fixed typo in example/pom.xml description ("strarter" → "starter")
- Extended default excludeHeaders list with security-sensitive headers

### Security
- Response headers containing sensitive information (Set-Cookie, Authorization, etc.) are now redacted by default
- Added comprehensive list of security-sensitive headers to default exclusion list

## [0.0.1-SNAPSHOT] - Initial Release

### Added
- Basic HTTP request capture functionality
- In-memory request storage
- Dashboard UI for viewing captured requests
- MockMvc test code generation
- Thymeleaf-based dashboard templates
- Spring Boot auto-configuration support
- Configurable path and header exclusions

[Unreleased]: https://github.com/patrickhofmann0/replay-spring-boot-starter/compare/v0.0.1...HEAD
[0.0.1-SNAPSHOT]: https://github.com/patrickhofmann0/replay-spring-boot-starter/releases/tag/v0.0.1
