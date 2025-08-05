#!/bin/bash
# Qute Template Validation for Native Build Compatibility
# Checks Qute templates for patterns that may break native compilation

set -e

EXIT_CODE=0
ISSUES_FOUND=0

echo "🔍 Checking Qute Template Native Compatibility..."

# Function to report issues
report_issue() {
    local file="$1"
    local line="$2"
    local issue="$3"
    echo "❌ $file:$line - $issue"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
    EXIT_CODE=1
}

# Function to check for dynamic class references
check_dynamic_class_refs() {
    local file="$1"
    
    # Check for dynamic class loading in templates
    if grep -n "class\.forName\|Class\.forName" "$file" > /dev/null; then
        local line_num=$(grep -n "class\.forName\|Class\.forName" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Dynamic class loading in template - not supported in native mode"
    fi
}

# Function to check for reflection usage in templates
check_template_reflection() {
    local file="$1"
    
    # Check for reflection method calls
    if grep -n "\.getClass()\." "$file" > /dev/null; then
        local line_num=$(grep -n "\.getClass()\." "$file" | cut -d: -f1 | head -1)
        echo "⚠️  $file:$line_num - getClass() usage detected - ensure target classes are registered for reflection"
    fi
    
    # Check for method invocation patterns that might use reflection
    if grep -n "invoke\|call\|execute" "$file" > /dev/null; then
        local line_num=$(grep -n "invoke\|call\|execute" "$file" | cut -d: -f1 | head -1)
        echo "ℹ️  $file:$line_num - Method invocation detected - verify native compatibility"
    fi
}

# Function to validate Qute syntax
check_qute_syntax() {
    local file="$1"
    
    # Check for proper Qute expression syntax
    if grep -n "{[^}]*[^{]}" "$file" > /dev/null; then
        # Check for unclosed expressions
        local unclosed=$(grep -o "{[^}]*$" "$file" | wc -l)
        if [[ $unclosed -gt 0 ]]; then
            report_issue "$file" "?" "Potentially unclosed Qute expressions found"
        fi
    fi
    
    # Check for nested expressions that might be problematic
    if grep -n "{{.*{{" "$file" > /dev/null; then
        local line_num=$(grep -n "{{.*{{" "$file" | cut -d: -f1 | head -1)
        echo "⚠️  $file:$line_num - Nested Qute expressions detected - verify syntax"
    fi
}

# Function to check for unsupported template features
check_unsupported_features() {
    local file="$1"
    
    # Check for JavaScript execution in templates
    if grep -n "<script.*eval\|<script.*Function" "$file" > /dev/null; then
        local line_num=$(grep -n "<script.*eval\|<script.*Function" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "Dynamic JavaScript execution detected - not supported in native mode"
    fi
    
    # Check for server-side includes that might be problematic
    if grep -n "<!--#include\|{#include" "$file" > /dev/null; then
        local line_num=$(grep -n "<!--#include\|{#include" "$file" | cut -d: -f1 | head -1)
        echo "ℹ️  $file:$line_num - Template include detected - ensure included templates are available at build time"
    fi
}

# Function to check for proper data binding
check_data_binding() {
    local file="$1"
    
    # Check for complex object navigation that might need reflection
    if grep -n "{[^}]*\.[^}]*\.[^}]*}" "$file" > /dev/null; then
        local line_num=$(grep -n "{[^}]*\.[^}]*\.[^}]*}" "$file" | cut -d: -f1 | head -1)
        echo "ℹ️  $file:$line_num - Complex object navigation detected - ensure all accessed properties are reflection-registered"
    fi
}

# Function to validate template structure
check_template_structure() {
    local file="$1"
    
    # Check for proper HTML structure (basic validation)
    if ! grep -q "<!DOCTYPE\|<html" "$file"; then
        echo "ℹ️  $file - No DOCTYPE or html tag found - consider adding for better compatibility"
    fi
    
    # Check for Qute template inheritance
    if grep -n "{#extends\|{#include" "$file" > /dev/null; then
        echo "ℹ️  $file - Template inheritance/inclusion detected - ensure all referenced templates exist"
    fi
}

# Main checking function
check_template() {
    local file="$1"
    
    if [[ ! -f "$file" ]]; then
        return
    fi
    
    echo "  Checking template: $file"
    
    check_dynamic_class_refs "$file"
    check_template_reflection "$file"
    check_qute_syntax "$file"
    check_unsupported_features "$file"
    check_data_binding "$file"
    check_template_structure "$file"
}

# Process all provided files
if [[ $# -eq 0 ]]; then
    echo "No template files provided for checking"
    exit 0
fi

for file in "$@"; do
    if [[ "$file" == *.html ]] || [[ "$file" == *.qute ]] || [[ "$file" == *.txt ]]; then
        check_template "$file"
    fi
done

# Summary
echo ""
if [[ $ISSUES_FOUND -eq 0 ]]; then
    echo "✅ No critical Qute template issues found!"
else
    echo "❌ Found $ISSUES_FOUND potential Qute template issues"
    echo ""
    echo "💡 Tips for fixing:"
    echo "  - Ensure all accessed object properties are registered for reflection"
    echo "  - Use static template includes instead of dynamic ones"
    echo "  - Avoid complex JavaScript execution in templates"
    echo "  - Test templates with native build before deployment"
fi

exit $EXIT_CODE
