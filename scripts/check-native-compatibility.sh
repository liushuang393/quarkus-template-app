#!/bin/bash
# Quarkus Native Build Compatibility Checker
# Checks Java files for patterns that may break native compilation

set -e

EXIT_CODE=0
ISSUES_FOUND=0

echo "🔍 Checking Quarkus Native Build Compatibility..."

# Function to report issues
report_issue() {
    local file="$1"
    local line="$2"
    local issue="$3"
    echo "❌ $file:$line - $issue"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
    EXIT_CODE=1
}

# Function to check for problematic reflection patterns
check_reflection_patterns() {
    local file="$1"
    
    # Check for Class.forName without @RegisterForReflection
    if grep -n "Class\.forName" "$file" > /dev/null; then
        local line_num=$(grep -n "Class\.forName" "$file" | cut -d: -f1 | head -1)
        # Check if @RegisterForReflection is present in the same file
        if ! grep -q "@RegisterForReflection" "$file"; then
            report_issue "$file" "$line_num" "Class.forName used without @RegisterForReflection annotation"
        fi
    fi
    
    # Check for Method.invoke without proper configuration
    if grep -n "\.invoke(" "$file" > /dev/null; then
        local line_num=$(grep -n "\.invoke(" "$file" | cut -d: -f1 | head -1)
        if ! grep -q "@RegisterForReflection" "$file" && ! grep -q "reflection-config.json" "$file"; then
            report_issue "$file" "$line_num" "Method.invoke used without reflection configuration"
        fi
    fi
    
    # Check for Constructor.newInstance
    if grep -n "Constructor.*newInstance" "$file" > /dev/null; then
        local line_num=$(grep -n "Constructor.*newInstance" "$file" | cut -d: -f1 | head -1)
        if ! grep -q "@RegisterForReflection" "$file"; then
            report_issue "$file" "$line_num" "Constructor.newInstance used without @RegisterForReflection"
        fi
    fi
}

# Function to check for problematic dynamic proxy usage
check_dynamic_proxy() {
    local file="$1"
    
    if grep -n "Proxy\.newProxyInstance" "$file" > /dev/null; then
        local line_num=$(grep -n "Proxy\.newProxyInstance" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Dynamic proxy usage detected - may not work in native mode"
    fi
}

# Function to check for problematic classloader usage
check_classloader_usage() {
    local file="$1"
    
    if grep -n "URLClassLoader\|getContextClassLoader" "$file" > /dev/null; then
        local line_num=$(grep -n "URLClassLoader\|getContextClassLoader" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Dynamic classloading detected - may not work in native mode"
    fi
}

# Function to check for unsupported JNI usage
check_jni_usage() {
    local file="$1"
    
    if grep -n "System\.loadLibrary\|System\.load" "$file" > /dev/null; then
        local line_num=$(grep -n "System\.loadLibrary\|System\.load" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "JNI library loading detected - verify GraalVM compatibility"
    fi
}

# Function to check for problematic serialization
check_serialization() {
    local file="$1"
    
    if grep -n "ObjectInputStream\|ObjectOutputStream" "$file" > /dev/null; then
        local line_num=$(grep -n "ObjectInputStream\|ObjectOutputStream" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Java serialization detected - consider using JSON/Jackson instead"
    fi
}

# Function to check for missing native annotations
check_native_annotations() {
    local file="$1"
    
    # Check if file uses reflection but lacks proper annotations
    if grep -q "getDeclaredMethod\|getDeclaredField\|getDeclaredConstructor" "$file"; then
        if ! grep -q "@RegisterForReflection\|@ReflectiveAccess" "$file"; then
            local line_num=$(grep -n "getDeclaredMethod\|getDeclaredField\|getDeclaredConstructor" "$file" | cut -d: -f1 | head -1)
            report_issue "$file" "$line_num" "Reflection usage without proper native annotations"
        fi
    fi
}

# Main checking function
check_file() {
    local file="$1"
    
    if [[ ! -f "$file" ]]; then
        return
    fi
    
    echo "  Checking: $file"
    
    check_reflection_patterns "$file"
    check_dynamic_proxy "$file"
    check_classloader_usage "$file"
    check_jni_usage "$file"
    check_serialization "$file"
    check_native_annotations "$file"
}

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
    echo "✅ No native compatibility issues found!"
else
    echo "❌ Found $ISSUES_FOUND potential native compatibility issues"
    echo ""
    echo "💡 Tips for fixing:"
    echo "  - Add @RegisterForReflection to classes using reflection"
    echo "  - Use Quarkus extensions instead of raw JDK APIs when possible"
    echo "  - Check GraalVM compatibility for third-party libraries"
    echo "  - Consider using compile-time reflection with Quarkus"
fi

exit $EXIT_CODE
