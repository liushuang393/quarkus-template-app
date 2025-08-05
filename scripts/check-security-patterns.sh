#!/bin/bash
# Basic Security Pattern Checker for Java files
# Checks for common security anti-patterns and vulnerabilities

set -e

EXIT_CODE=0
ISSUES_FOUND=0

echo "🔍 Checking Security Patterns..."

# Function to report issues
report_issue() {
    local file="$1"
    local line="$2"
    local severity="$3"
    local issue="$4"
    echo "❌ [$severity] $file:$line - $issue"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
    if [[ "$severity" == "HIGH" ]] || [[ "$severity" == "CRITICAL" ]]; then
        EXIT_CODE=1
    fi
}

# Function to check for SQL injection patterns
check_sql_injection() {
    local file="$1"

    # Check for string concatenation in SQL queries
    if grep -n "\".*SELECT.*\" *+" "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "\".*SELECT.*\" *+" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "HIGH" "Potential SQL injection - string concatenation in SQL query"
    fi

    # Check for direct parameter insertion
    if grep -n "Statement.*executeQuery.*+" "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "Statement.*executeQuery.*+" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "HIGH" "Potential SQL injection - use PreparedStatement instead"
    fi
}

# Function to check for hardcoded credentials
check_hardcoded_credentials() {
    local file="$1"

    # Check for hardcoded passwords (excluding database field mappings and SQL queries)
    # Skip lines that contain @Result, @Select, @Insert, @Update, @Delete annotations
    # Skip lines that are SQL queries or database field mappings

    local patterns=("password.*=.*\"[^\"]{8,}\"" "pwd.*=.*\"[^\"]{4,}\"" "secret.*=.*\"[^\"]{8,}\"" "token.*=.*\"[^\"]{16,}\"" "key.*=.*\"[^\"]{8,}\"")

    for pattern in "${patterns[@]}"; do
        # Exclude database annotations and SQL queries
        if grep -ni "$pattern" "$file" | grep -v "@Result\|@Select\|@Insert\|@Update\|@Delete\|SELECT\|INSERT\|UPDATE\|FROM\|WHERE" > /dev/null; then
            local line_num
            line_num=$(grep -ni "$pattern" "$file" | grep -v "@Result\|@Select\|@Insert\|@Update\|@Delete\|SELECT\|INSERT\|UPDATE\|FROM\|WHERE" | cut -d: -f1 | head -1)
            if [[ -n "$line_num" ]]; then
                report_issue "$file" "$line_num" "CRITICAL" "Hardcoded credential detected"
            fi
        fi
    done
}

# Function to check for insecure random number generation
check_insecure_random() {
    local file="$1"

    if grep -n "Math\.random\|Random()" "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "Math\.random\|Random()" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "MEDIUM" "Insecure random number generation - use SecureRandom for security-sensitive operations"
    fi
}

# Function to check for unsafe deserialization
check_unsafe_deserialization() {
    local file="$1"

    if grep -n "ObjectInputStream.*readObject" "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "ObjectInputStream.*readObject" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "HIGH" "Unsafe deserialization - validate input before deserializing"
    fi
}

# Function to check for path traversal vulnerabilities
check_path_traversal() {
    local file="$1"

    if grep -n "new File.*\.\." "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "new File.*\.\." "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "HIGH" "Potential path traversal vulnerability"
    fi
}

# Function to check for weak cryptography
check_weak_crypto() {
    local file="$1"

    # Check for weak algorithms
    local weak_algos=("MD5" "SHA1" "DES" "RC4")

    for algo in "${weak_algos[@]}"; do
        if grep -n "$algo" "$file" > /dev/null; then
            local line_num
            line_num=$(grep -n "$algo" "$file" | cut -d: -f1 | head -1)
            report_issue "$file" "$line_num" "MEDIUM" "Weak cryptographic algorithm: $algo"
        fi
    done
}

# Function to check for unsafe HTTP usage
check_unsafe_http() {
    local file="$1"

    if grep -n "http://" "$file" > /dev/null; then
        local line_num
        line_num=$(grep -n "http://" "$file" | cut -d: -f1 | head -1)
        report_issue "$file" "$line_num" "LOW" "Insecure HTTP protocol - consider using HTTPS"
    fi
}

# Function to check if file should be excluded
should_exclude_file() {
    local file="$1"

    # Exclude build and dependency directories
    if [[ "$file" == *"target/"* ]] || \
       [[ "$file" == *".mvn/"* ]] || \
       [[ "$file" == *"node_modules/"* ]] || \
       [[ "$file" == *"vendor/"* ]] || \
       [[ "$file" == *"build/"* ]] || \
       [[ "$file" == *"dist/"* ]]; then
        return 0  # Should exclude
    fi

    # Exclude IDE and editor files
    if [[ "$file" == *".idea/"* ]] || \
       [[ "$file" == *".vscode/"* ]] || \
       [[ "$file" == *".settings/"* ]] || \
       [[ "$file" == *".eclipse/"* ]]; then
        return 0  # Should exclude
    fi

    # Exclude version control
    if [[ "$file" == *".git/"* ]] || \
       [[ "$file" == *".svn/"* ]] || \
       [[ "$file" == *".hg/"* ]]; then
        return 0  # Should exclude
    fi

    # Exclude third-party generated code
    if [[ "$file" == *"MavenWrapperDownloader.java"* ]] || \
       [[ "$file" == *"/generated/"* ]] || \
       [[ "$file" == *"/generated-sources/"* ]] || \
       [[ "$file" == *"/generated-test-sources/"* ]]; then
        return 0  # Should exclude
    fi

    # Exclude specific file types that shouldn't be checked
    if [[ "$file" == *.jar ]] || \
       [[ "$file" == *.war ]] || \
       [[ "$file" == *.ear ]] || \
       [[ "$file" == *.class ]] || \
       [[ "$file" == *.log ]] || \
       [[ "$file" == *.cache ]] || \
       [[ "$file" == *.lock ]] || \
       [[ "$file" == *.tmp ]] || \
       [[ "$file" == *.temp ]]; then
        return 0  # Should exclude
    fi

    # Exclude image and media files
    if [[ "$file" == *.png ]] || \
       [[ "$file" == *.jpg ]] || \
       [[ "$file" == *.jpeg ]] || \
       [[ "$file" == *.gif ]] || \
       [[ "$file" == *.svg ]] || \
       [[ "$file" == *.ico ]] || \
       [[ "$file" == *.pdf ]]; then
        return 0  # Should exclude
    fi

    # Exclude certificate and key files
    if [[ "$file" == *.crt ]] || \
       [[ "$file" == *.key ]] || \
       [[ "$file" == *.pem ]] || \
       [[ "$file" == *.p12 ]] || \
       [[ "$file" == *.jks ]]; then
        return 0  # Should exclude
    fi

    # Exclude OS-specific files
    if [[ "$file" == *".DS_Store"* ]] || \
       [[ "$file" == *"Thumbs.db"* ]] || \
       [[ "$file" == *.swp ]] || \
       [[ "$file" == *.swo ]]; then
        return 0  # Should exclude
    fi

    return 1  # Should not exclude
}

# Main checking function
check_file() {
    local file="$1"

    if [[ ! -f "$file" ]]; then
        return
    fi

    # Skip excluded files
    if should_exclude_file "$file"; then
        echo "  Skipping (third-party): $file"
        return
    fi

    echo "  Checking: $file"

    check_sql_injection "$file"
    check_hardcoded_credentials "$file"
    check_insecure_random "$file"
    check_unsafe_deserialization "$file"
    check_path_traversal "$file"
    check_weak_crypto "$file"
    check_unsafe_http "$file"
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
    echo "✅ No security issues found!"
else
    echo "❌ Found $ISSUES_FOUND potential security issues"
    echo ""
    echo "💡 Security recommendations:"
    echo "  - Use parameterized queries to prevent SQL injection"
    echo "  - Store credentials in environment variables or secure vaults"
    echo "  - Use SecureRandom for cryptographic operations"
    echo "  - Validate and sanitize all user inputs"
    echo "  - Use strong cryptographic algorithms (AES, SHA-256+)"
    echo "  - Always use HTTPS for network communications"
fi

exit $EXIT_CODE
