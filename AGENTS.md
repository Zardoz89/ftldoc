# AGENTS.md - Agent Guidelines for ftldoc

## Project Overview

ftldoc is a Maven plugin that generates HTML documentation for FreeMarker macro libraries (`.ftl` files). It is a mixed Java/Groovy project using Maven as the build system.

## Build Commands

### Compile and Test
```bash
mvn clean compile       # Compile main sources
mvn clean test          # Compile and run tests
mvn clean verify       # Full verification (includes tests)
mvn clean install      # Install locally (includes tests + packaging)
```

### Running a Single Test

**Spock (Groovy) tests:**
```bash
mvn test -Dtest=ParseFtlDocCommentSpec
mvn test -Dtest=ParseFtlDocCommentSpec#Parsing*comment
```

**JUnit tests (if added):**
```bash
mvn test -Dtest=ClassName
mvn test -Dtest=ClassName#testMethodName
```

### Other Useful Commands
```bash
mvn clean package      # Package (skip tests)
mvn generate-sources   # Generate sources only
mvn javadoc:javadoc    # Generate Javadoc
mvn dependency:tree    # Show dependencies
```

### Using mvnd (Maven Daemon)

If `mvnd` is installed on the system, it can be used instead of `mvn` for faster builds. It runs Maven in a daemon and caches classloaders and compiled code.

```bash
mvnd clean compile     # Compile main sources
mvnd clean test        # Compile and run tests
mvnd clean install     # Install locally
mvnd test -Dtest=ParseFtlDocCommentSpec  # Run single test
```

Note: Some commands may differ slightly. Check `mvnd --help` for options.

## Code Style Guidelines

### General Conventions
- **Language:** Java 8 (1.8) with Groovy for tests
- **Indentation:** 4 spaces (no tabs)
- **Line length:** No hard limit, but keep lines reasonable (~100-120 chars)
- **Braces:** K&R style (opening brace on same line)

### Naming Conventions
- **Classes/Interfaces:** PascalCase (e.g., `FtlDoc`, `ParseFtlDocComment`)
- **Methods:** camelCase (e.g., `createFilePage`, `parse`)
- **Fields/Variables:** camelCase (e.g., `outputDir`, `freemarkerFiles`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `OUTPUT_ENCODING`)
- **Packages:** lowercase, hierarchical (e.g., `freemarker.tools.ftldoc`)

### Imports Organization
Order imports by:
1. `java.*` packages
2. `javax.*` packages  
3. Third-party org/apache packages
4. Project packages (`freemarker.*`)

### Java Code Style
- **Visibility:** Use minimum visibility needed (`private` > `package-private` > `protected` > `public`)
- **Final:** Use `final` for parameters and fields where appropriate
- **Types:** Use interfaces over concrete types where possible (e.g., `List<File>` vs `ArrayList<File>`)
- **Annotations:** Use annotations for Mojo parameters (`@Parameter`, `@Mojo`)

### Javadoc
- Required for public classes and methods
- Use `@param`, `@return`, `@throws` where applicable
- First sentence should be a concise summary

### Error Handling
- Use try-with-resources for closeable resources
- Catch specific exceptions when possible, but generic `Exception` is acceptable forMojo execution
- Always log errors: `log.error(ex)` or `log.error("message", ex)`

### Test Conventions (Spock/Groovy)
- Test class naming: `*Spec.groovy` (Spock Specification)
- Test method naming: Use descriptive strings with spaces: `def "description"() {}`
- Use `@Unroll` for parameterized tests
- Use `given/when/then` blocks for clarity

### Groovy-Specific
- Use Groovy's `def` for local variables
- Use GString for string interpolation: `"value: ${variable}"`
- Spock 2.x uses `spock.lang.*` annotations

## Project Structure

```
src/
├── main/
│   ├── java/freemarker/tools/ftldoc/
│   │   ├── FtlDocMojo.java        # Maven Mojo entry point
│   │   ├── FtlDoc.java            # Core documentation generator
│   │   ├── ParseFtlDocComment.java # Comment parser
│   │   ├── Logger.java             # Logging wrapper
│   │   ├── Templates.java          # Template constants
│   │   └── TemplateElementModel.java
│   └── resources/
│       └── default/               # Default FTL templates
└── test/
    └── groovy/freemarker/tools/ftldoc/
        ├── ParseFtlDocCommentSpec.groovy
        └── FtlDocSpec.groovy
```

## Key Dependencies

- **FreeMarker:** 2.3.31 (template engine)
- **Maven Plugin API:** 3.5.4
- **Apache Commons IO:** 2.8.0
- **Apache Commons Lang3:** 3.x (StringUtils)
- **JUnit Jupiter:** 5.7.1
- **Spock:** 2.0 (Groovy 2.5)

## Maven Plugin Configuration

The project is itself a Maven plugin. When testing locally:
```bash
mvn install -DskipTests  # Install plugin without running tests
```

## Important Notes

1. **Thread Safety:** The Mojo is annotated with `threadSafe = true` - ensure any new code maintains this
2. **FreeMarker Version:** Configurable via `freemarkerVersion` parameter (default 2.3.31)
3. **Template Loading:** Supports both default templates (bundled) and custom template directories
4. **Comment Syntax:** Comments start with `<#---` (3 dashes) to mimic Javadoc behavior

## IDE Integration

- Eclipse: Use m2e plugin, project uses Groovy-Eclipse-Compiler
- IntelliJ IDEA: Native Maven and Groovy support
- VS Code: Use Java and Groovy extensions

## Regenerating Examples

After making changes that affect output, regenerate the examples:

**Install the plugin locally first:**
```bash
mvn install -DskipTests
```

**Regenerate default_templates example (uses default FTL templates as source):**
```bash
mvn io.github.zardoz89:ftldoc-maven-plugin:0.1.3-SNAPSHOT:generate-documentation \
  -DfreemarkerFiles=src/main/resources/default/ \
  -DoutputDirectory=examples/default_templates \
  -DtemplateDirectory=src/main/resources/default
```

**Regenerate simple_test example (uses test FTL files as source):**
```bash
mvn io.github.zardoz89:ftldoc-maven-plugin:0.1.3-SNAPSHOT:generate-documentation \
  -DfreemarkerFiles=src/test/resources/test/ \
  -DoutputDirectory=examples/simple_test
```
