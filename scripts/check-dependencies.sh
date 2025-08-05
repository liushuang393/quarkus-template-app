#!/bin/bash
# Maven Dependency Security and Compatibility Checker
# Checks for security vulnerabilities and GraalVM compatibility

set -e

EXIT_CODE=0
ISSUES_FOUND=0

echo "🔍 Checking Maven Dependencies..."

# Function to report issues
report_issue() {
    local severity="$1"
    local issue="$2"
    echo "❌ [$severity] $issue"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
    if [[ "$severity" == "HIGH" ]] || [[ "$severity" == "CRITICAL" ]]; then
        EXIT_CODE=1
    fi
}

# Function to check for OWASP dependency vulnerabilities
check_owasp_vulnerabilities() {
    echo "  Running OWASP Dependency Check..."

    # Check if OWASP plugin is configured
    if grep -q "dependency-check-maven" pom.xml; then
        echo "✅ OWASP Dependency Check plugin found in pom.xml"

        # Run dependency check (suppress output for cleaner logs)
        if ./mvnw org.owasp:dependency-check-maven:check -q > /dev/null 2>&1; then
            echo "✅ No high-severity vulnerabilities found"
        else
            echo "⚠️  Potential vulnerabilities detected - check target/dependency-check-report.html"
        fi
    else
        echo "ℹ️  OWASP Dependency Check plugin not configured - consider adding it"
    fi
}

# Function to check for outdated dependencies
check_outdated_dependencies() {
    echo "  Checking for outdated dependencies..."

    # Use Maven versions plugin to check for updates
    if ./mvnw versions:display-dependency-updates -q 2>/dev/null | grep -q "The following dependencies"; then
        echo "⚠️  Outdated dependencies found - run 'mvn versions:display-dependency-updates' for details"
    else
        echo "✅ Dependencies are up to date"
    fi
}

# Function to check for known problematic dependencies in native mode
check_native_incompatible_deps() {
    echo "  Checking for native build incompatible dependencies..."

    # List of dependencies known to have issues with GraalVM native compilation
    local problematic_deps=(
        "spring-boot-starter"
        "hibernate-core:5\."
        "jackson-databind:2\.9\."
        "logback-classic:1\.1\."
        "junit:junit:4\."
    )

    for dep in "${problematic_deps[@]}"; do
        if grep -q "$dep" pom.xml; then
            echo "⚠️  Potentially problematic dependency for native build: $dep"
            echo "     Consider using Quarkus-compatible alternatives"
        fi
    done
}

# Function to check for Quarkus extension compatibility
check_quarkus_extensions() {
    echo "  Validating Quarkus extensions..."

    # Check if all io.quarkus dependencies are compatible versions
    local quarkus_version
    quarkus_version=$(grep -o "quarkus\.platform\.version>[^<]*" pom.xml | cut -d'>' -f2 | head -1)

    if [[ -n "$quarkus_version" ]]; then
        echo "✅ Quarkus platform version: $quarkus_version"

        # Check for mixed Quarkus versions
        if grep "io\.quarkus" pom.xml | grep -v "$quarkus_version" | grep -q "version"; then
            echo "⚠️  Mixed Quarkus versions detected - ensure all extensions use platform BOM"
        fi
    else
        echo "⚠️  Quarkus platform version not found in pom.xml"
    fi
}

# Function to check for security-sensitive dependencies
check_security_sensitive_deps() {
    echo "  Checking for security-sensitive dependencies..."

    local security_deps=(
        "commons-collections:3\."
        "struts"
        "log4j:1\."
        "spring-core:4\."
    )

    for dep in "${security_deps[@]}"; do
        if grep -q "$dep" pom.xml; then
            report_issue "HIGH" "Security-sensitive dependency detected: $dep"
        fi
    done
}

# Function to check dependency scopes
check_dependency_scopes() {
    echo "  Validating dependency scopes..."

    # Check for test dependencies in compile scope
    if grep -A 2 "junit\|mockito\|testcontainers" pom.xml | grep -v "test" | grep -q "scope"; then
        echo "⚠️  Test dependencies may be in wrong scope"
    fi

    # Check for provided dependencies that should be compile
    if grep -A 2 "quarkus-" pom.xml | grep -q "<scope>provided</scope>"; then
        echo "⚠️  Quarkus dependencies should not use 'provided' scope"
    fi
}

# Function to validate Maven configuration
check_maven_config() {
    echo "  Validating Maven configuration..."

    # Check Java version compatibility
    if grep -q "<maven.compiler.source>21</maven.compiler.source>" pom.xml; then
        echo "✅ Java 21 source compatibility configured"
    else
        echo "⚠️  Java 21 source compatibility not explicitly set"
    fi

    # Check for proper encoding
    if grep -q "<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>" pom.xml; then
        echo "✅ UTF-8 encoding configured"
    else
        echo "⚠️  UTF-8 encoding not explicitly set"
    fi
}

# Main execution
if [[ ! -f "pom.xml" ]]; then
    echo "❌ pom.xml not found in current directory"
    exit 1
fi

check_maven_config
check_quarkus_extensions
check_native_incompatible_deps
check_security_sensitive_deps
check_dependency_scopes
check_outdated_dependencies
check_owasp_vulnerabilities

# Summary
echo ""
if [[ $ISSUES_FOUND -eq 0 ]]; then
    echo "✅ No critical dependency issues found!"
else
    echo "❌ Found $ISSUES_FOUND potential dependency issues"
    echo ""
    echo "💡 Recommendations:"
    echo "  - Update outdated dependencies regularly"
    echo "  - Use Quarkus BOM for version management"
    echo "  - Test native builds with updated dependencies"
    echo "  - Monitor security advisories for used dependencies"
fi

exit $EXIT_CODE
