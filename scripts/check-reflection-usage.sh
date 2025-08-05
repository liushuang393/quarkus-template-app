#!/bin/bash
# Reflection Usage Checker for Quarkus Native Build
# Validates that reflection usage is properly configured for native compilation

set -e

EXIT_CODE=0
ISSUES_FOUND=0

echo "🔍 Checking Reflection Configuration..."

# Function to report issues
report_issue() {
    local file="$1"
    local line="$2"
    local issue="$3"
    echo "❌ $file:$line - $issue"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
    EXIT_CODE=1
}

# Function to check if reflection config exists
check_reflection_config_exists() {
    local config_files=(
        "src/main/resources/META-INF/native-image/reflect-config.json"
        "src/main/resources/META-INF/native-image/reflection-config.json"
        "src/main/resources/reflection-config.json"
    )

    local config_found=false
    for config_file in "${config_files[@]}"; do
        if [[ -f "$config_file" ]]; then
            config_found=true
            echo "✅ Found reflection config: $config_file"
            break
        fi
    done

    return 0  # Don't fail if no config file exists, as Quarkus handles most cases
}

# Function to check for reflection usage patterns
check_reflection_usage() {
    local file="$1"

    # Patterns that require reflection configuration
    local reflection_patterns=(
        "Class\.forName"
        "\.getDeclaredMethod"
        "\.getDeclaredField"
        "\.getDeclaredConstructor"
        "\.getMethod"
        "\.getField"
        "\.getConstructor"
        "\.newInstance"
        "Method\.invoke"
        "Field\.set"
        "Field\.get"
        "Constructor\.newInstance"
    )

    for pattern in "${reflection_patterns[@]}"; do
        if grep -n "$pattern" "$file" > /dev/null; then
            local line_num=$(grep -n "$pattern" "$file" | cut -d: -f1 | head -1)
            local class_name=$(basename "$file" .java)

            # Check if the class has @RegisterForReflection annotation
            if grep -q "@RegisterForReflection" "$file"; then
                echo "✅ $file:$line_num - $pattern usage with @RegisterForReflection annotation"
            else
                # Check if it's in a Quarkus extension or framework code (usually safe)
                if grep -q "@Entity\|@RestController\|@ApplicationScoped\|@RequestScoped\|@Singleton" "$file"; then
                    echo "ℹ️  $file:$line_num - $pattern usage in Quarkus managed class (likely safe)"
                else
                    echo "⚠️  $file:$line_num - $pattern usage without @RegisterForReflection - consider adding annotation"
                fi
            fi
        fi
    done
}

# Function to check for annotation-based reflection
check_annotation_reflection() {
    local file="$1"

    # Check for annotations that might use reflection
    local annotation_patterns=(
        "@JsonProperty"
        "@JsonIgnore"
        "@XmlElement"
        "@Column"
        "@JoinColumn"
        "@OneToMany"
        "@ManyToOne"
        "@ManyToMany"
        "@OneToOne"
    )

    for pattern in "${annotation_patterns[@]}"; do
        if grep -n "$pattern" "$file" > /dev/null; then
            local line_num=$(grep -n "$pattern" "$file" | cut -d: -f1 | head -1)
            echo "ℹ️  $file:$line_num - $pattern annotation detected - Quarkus should handle reflection automatically"
        fi
    done
}

# Function to check for proper @RegisterForReflection usage
check_register_for_reflection() {
    local file="$1"

    if grep -n "@RegisterForReflection" "$file" > /dev/null; then
        local line_num=$(grep -n "@RegisterForReflection" "$file" | cut -d: -f1 | head -1)
        echo "✅ $file:$line_num - @RegisterForReflection annotation found"

        # Check if it specifies targets properly
        if grep -A 5 "@RegisterForReflection" "$file" | grep -q "targets\|value"; then
            echo "✅ $file - @RegisterForReflection with explicit targets"
        fi
    fi
}

# Function to check for problematic reflection patterns
check_problematic_patterns() {
    local file="$1"

    # Check for dynamic class name construction
    if grep -n "Class\.forName.*+\|Class\.forName.*concat" "$file" > /dev/null; then
        local line_num=$(grep -n "Class\.forName.*+\|Class\.forName.*concat" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Dynamic class name construction detected - may not work in native mode"
    fi

    # Check for reflection on generic types
    if grep -n "getGenericType\|getParameterizedType" "$file" > /dev/null; then
        local line_num=$(grep -n "getGenericType\|getParameterizedType" "$file" | cut -d: -f1 | head -1)
        echo "⚠️  $file:$line_num - Generic type reflection detected - verify native compatibility"
    fi
}

# Main checking function
check_file() {
    local file="$1"

    if [[ ! -f "$file" ]]; then
        return
    fi

    echo "  Checking: $file"

    check_reflection_usage "$file"
    check_annotation_reflection "$file"
    check_register_for_reflection "$file"
    check_problematic_patterns "$file"
}

# Check for reflection configuration files
check_reflection_config_exists

# Process all provided files
if [[ $# -eq 0 ]]; then
    echo "No files provided for checking"
    exit 0
fi

for file in "$@"; do
    if [[ "$file" == *.java ]]; then
        check_file "$file"
    fi
done

# Summary
echo ""
if [[ $ISSUES_FOUND -eq 0 ]]; then
    echo "✅ No critical reflection configuration issues found!"
else
    echo "❌ Found $ISSUES_FOUND potential reflection configuration issues"
fi

echo ""
echo "💡 Reflection Best Practices for Quarkus Native:"
echo "  - Use @RegisterForReflection on classes that need reflection"
echo "  - Prefer Quarkus extensions over raw JDK reflection APIs"
echo "  - Test with native build to catch reflection issues early"
echo "  - Use build-time reflection when possible"

exit $EXIT_CODE
