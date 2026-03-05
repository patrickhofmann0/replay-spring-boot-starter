# Contributing to Replay Spring Boot Starter

Thank you for your interest in contributing to the Replay Spring Boot Starter project! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

This project adheres to a code of conduct that all contributors are expected to uphold. Please be respectful and constructive in all interactions.

## How Can I Contribute?

### Reporting Bugs

Before submitting a bug report:
- Check the [issue tracker](https://github.com/patrickhofmann0/replay-spring-boot-starter/issues) to avoid duplicates
- Collect relevant information (Spring Boot version, Java version, stacktraces, etc.)

When submitting a bug report, include:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs. actual behavior
- Your environment details
- Any relevant code samples or error messages

### Suggesting Enhancements

Enhancement suggestions are welcome! Please:
- Use a clear, descriptive title
- Provide a detailed description of the proposed functionality
- Explain why this enhancement would be useful
- Consider including code examples or mockups

### Contributing Code

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add or update tests as needed
5. Ensure all tests pass
6. Commit your changes
7. Push to your branch
8. Open a Pull Request

## Development Setup

### Prerequisites

- Java 21 or later
- Maven 3.6 or later
- Git

### Building the Project

```bash
git clone https://github.com/patrickhofmann0/replay-spring-boot-starter.git
cd replay-spring-boot-starter
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Running the Example Application

```bash
cd example
mvn spring-boot:run
```

## Pull Request Process

1. **Update Documentation**: Ensure the README.md and other documentation reflects any changes
2. **Add Tests**: New features should include appropriate test coverage
3. **Follow Coding Standards**: Adhere to the project's coding style
4. **Update CHANGELOG**: Add an entry describing your changes
5. **Single Responsibility**: Keep PRs focused on a single feature or fix
6. **Descriptive Title**: Use a clear, concise title that summarizes the changes
7. **Detailed Description**: Explain what changes were made and why

### PR Review Process

- At least one maintainer review is required
- All tests must pass
- Code coverage should not decrease
- Code quality checks must pass

## Coding Standards

### Java Code Style

- Use 4 spaces for indentation (no tabs)
- Follow standard Java naming conventions
- Keep methods focused and small
- Add JavaDoc comments for public APIs
- Use meaningful variable and method names

### Project Structure

```
src/
├── main/
│   ├── java/de/hofmannhbm/replay/
│   │   ├── config/          # Configuration classes
│   │   ├── core/            # Core functionality
│   │   └── generator/       # Test code generators
│   └── resources/
│       ├── META-INF/
│       └── templates/       # Thymeleaf templates
└── test/
    └── java/de/hofmannhbm/replay/  # Tests mirror main structure
```

## Testing Guidelines

### Test Coverage

- Aim for at least 80% code coverage
- Write unit tests for all public methods
- Include integration tests for key workflows
- Test edge cases and error conditions

### Test Naming

Use descriptive test method names that explain what is being tested:

```java
@Test
void shouldCaptureRequestWithValidInput() { }

@Test
void shouldThrowExceptionWhenInputIsNull() { }
```

### Test Structure

Follow the Arrange-Act-Assert pattern:

```java
@Test
void shouldDoSomething() {
    // Arrange
    var input = createTestInput();
    
    // Act
    var result = service.process(input);
    
    // Assert
    assertThat(result).isNotNull();
}
```

## Commit Message Guidelines

### Format

```
<type>: <subject>

<body>

<footer>
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Build process or tooling changes

### Examples

```
feat: add content-type filtering configuration

Add new configuration option 'replay.includeContentTypes' to allow
filtering captured requests by response content type.

Closes #123
```

```
fix: resolve thread-safety issue in findById method

Made findById() synchronized to prevent race conditions when
accessing the request storage concurrently.
```

### Subject Line Rules

- Use imperative mood ("add feature" not "added feature")
- Don't capitalize first letter
- No period at the end
- Limit to 50 characters

### Body

- Wrap at 72 characters
- Explain *what* and *why*, not *how*
- Reference issues and pull requests

## Questions?

If you have questions about contributing, feel free to:
- Open an issue for discussion
- Check existing issues and PRs for similar topics

Thank you for contributing! 
