import { useState, useEffect } from "react"

export default function CodeTemplatesTab({ formData, setFormData }) {
  const [activeLanguage, setActiveLanguage] = useState("JAVA")

  // Default templates - always use these as base for generation
  const defaultTemplates = {
    JAVA: {
      visibleCode: `public class Solution {
    public {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
}`,
      hiddenCode: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Solution solution = new Solution();
        runTestSuite(solution);
    }

    public static void runTestSuite(Solution solution) {
        System.out.println("Running Test Cases...");
        System.out.println("=====================");

        // Test cases will be added here

        System.out.println("=====================");
        System.out.println("Test execution completed");
    }
}`
    },
    PYTHON: {
      visibleCode: `class Solution:
    def {FUNCTION_NAME}(self{PARAMETERS}) -> {RETURN_TYPE}:
        # Write your code here
        pass`,
      hiddenCode: `# Test cases will be auto-generated based on problem test cases
def run_tests():
    solution = Solution()
    print("Running Test Cases...")
    print("=====================")

    # Test cases will be added here

    print("=====================")
    print("Test execution completed")

if __name__ == "__main__":
    run_tests()`
    },
    JAVASCRIPT: {
      visibleCode: `/**
 * @param {PARAMETERS}
 * @return {RETURN_TYPE}
 */
var {FUNCTION_NAME} = function({PARAMETERS}) {
    // Write your code here

};`,
      hiddenCode: `// Test cases will be auto-generated based on problem test cases
function runTests() {
    console.log("Running Test Cases...");
    console.log("=====================");

    // Test cases will be added here

    console.log("=====================");
    console.log("Test execution completed");
}

runTests();`
    },
    CPP: {
      visibleCode: `class Solution {
public:
    {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
};`,
      hiddenCode: `#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

// Test cases will be auto-generated based on problem test cases
void runTestSuite() {
    Solution solution;
    cout << "Running Test Cases..." << endl;
    cout << "=====================" << endl;

    // Test cases will be added here

    cout << "=====================" << endl;
    cout << "Test execution completed" << endl;
}

int main() {
    runTestSuite();
    return 0;
}`
    },
    C: {
      visibleCode: `{RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
    // Write your code here

}`,
      hiddenCode: `#include <stdio.h>
#include <stdlib.h>

// Test cases will be auto-generated based on problem test cases
void runTestSuite() {
    printf("Running Test Cases...\\n");
    printf("=====================\\n");

    // Test cases will be added here

    printf("=====================\\n");
    printf("Test execution completed\\n");
}

int main() {
    runTestSuite();
    return 0;
}`
    }
  }

  const [templates, setTemplates] = useState(defaultTemplates)

  const languages = [
    { key: "JAVA", label: "Java", icon: "☕" },
    { key: "PYTHON", label: "Python", icon: "🐍" },
    { key: "JAVASCRIPT", label: "JavaScript", icon: "🟨" },
    { key: "CPP", label: "C++", icon: "⚡" },
    { key: "C", label: "C", icon: "🔧" }
  ]


  useEffect(() => {
    if (formData.codeTemplates) {
      setTemplates(prevTemplates => {
        const merged = { ...prevTemplates }
        Object.keys(formData.codeTemplates).forEach(lang => {
          if (formData.codeTemplates[lang]) {
            merged[lang] = {
              visibleCode: formData.codeTemplates[lang].visibleCode || prevTemplates[lang]?.visibleCode || '',
              hiddenCode: formData.codeTemplates[lang].hiddenCode || prevTemplates[lang]?.hiddenCode || ''
            }
          }
        })
        return merged
      })
    }
  }, [formData.codeTemplates])

  const updateTemplate = (language, type, code) => {
    setTemplates(prevTemplates => {
      const updatedTemplates = {
        ...prevTemplates,
        [language]: {
          ...prevTemplates[language],
          [type]: code
        }
      }

      return updatedTemplates
    })

    setFormData(prevFormData => ({
      ...prevFormData,
      codeTemplates: {
        ...prevFormData.codeTemplates,
        [language]: {
          ...prevFormData.codeTemplates?.[language],
          [type]: code
        }
      }
    }))
  }

  const mapTypeToLanguage = (genericType, language) => {
    const typeMap = {
      string: {
        JAVA: 'String',
        PYTHON: 'str',
        JAVASCRIPT: 'string',
        CPP: 'std::string',
        C: 'char*'
      },
      'string[]': {
        JAVA: 'String[]',
        PYTHON: 'List[str]',
        JAVASCRIPT: 'string[]',
        CPP: 'std::vector<std::string>',
        C: 'char**'
      },
      number: {
        JAVA: 'int',
        PYTHON: 'int',
        JAVASCRIPT: 'number',
        CPP: 'int',
        C: 'int'
      },
      'number[]': {
        JAVA: 'int[]',
        PYTHON: 'List[int]',
        JAVASCRIPT: 'number[]',
        CPP: 'std::vector<int>',
        C: 'int*'
      },
      boolean: {
        JAVA: 'boolean',
        PYTHON: 'bool',
        JAVASCRIPT: 'boolean',
        CPP: 'bool',
        C: 'int'
      },
      'boolean[]': {
        JAVA: 'boolean[]',
        PYTHON: 'List[bool]',
        JAVASCRIPT: 'boolean[]',
        CPP: 'std::vector<bool>',
        C: 'int*'
      },
      any: {
        JAVA: 'Object',
        PYTHON: 'Any',
        JAVASCRIPT: 'any',
        CPP: 'auto',
        C: 'void*'
      },
      'List<number>': {
        JAVA: 'List<Integer>',
        PYTHON: 'List[int]',
        JAVASCRIPT: 'number[]',
        CPP: 'std::vector<int>',
        C: 'int*'
      },
      'List<string>': {
        JAVA: 'List<String>',
        PYTHON: 'List[str]',
        JAVASCRIPT: 'string[]',
        CPP: 'std::vector<std::string>',
        C: 'char**'
      },
      void: {
        JAVA: 'void',
        PYTHON: 'None',
        JAVASCRIPT: 'void',
        CPP: 'void',
        C: 'void'
      }
    }
    return typeMap[genericType]?.[language] || genericType
  }

  // FIXED: Better parameter parsing with validation
  const parseParameters = () => {
    try {
      if (formData.parameters) {
        if (typeof formData.parameters === 'string' && formData.parameters.trim()) {
          const parsed = JSON.parse(formData.parameters)
          return Array.isArray(parsed) ? parsed : []
        } else if (Array.isArray(formData.parameters)) {
          return formData.parameters
        }
      }
    } catch (error) {
      console.warn('Parameter parsing failed, using empty array')
    }
    return []
  }

  // FIXED: Completely rewritten parameter formatting
  const formatParams = (language, params) => {
    if (!params || params.length === 0) {
      // Return appropriate empty values for each language
      switch (language) {
        case 'PYTHON':
          return '' // No parameters for Python (self is handled separately)
        case 'JAVASCRIPT':
          return ''
        default:
          return ''
      }
    }

    const mappedParams = params.map(p => ({
      ...p,
      type: mapTypeToLanguage(p.type, language)
    }))
    
    switch (language) {
      case 'JAVA':
        return mappedParams.map(p => `${p.type} ${p.name}`).join(', ')
      case 'PYTHON':
        // FIXED: Only include user parameters, self is already in template
        return mappedParams.map(p => p.name).join(', ')
      case 'JAVASCRIPT':
        return mappedParams.map(p => p.name).join(', ')
      case 'CPP':
        return mappedParams.map(p => `${p.type} ${p.name}`).join(', ')
      case 'C':
        return mappedParams.map(p => `${p.type} ${p.name}`).join(', ')
      default:
        return mappedParams.map(p => p.name).join(', ')
    }
  }

  // FIXED: Better JSDoc parameter formatting
  const formatJSDocParams = (params) => {
    if (!params || params.length === 0) {
      return ''
    }
    return params.map(p => {
      const jsType = mapTypeToLanguage(p.type, 'JAVASCRIPT')
      return `${p.name} {${jsType}}`
    }).join(', ')
  }

  // NEW: Validation function
  const validateFormData = () => {
    const errors = []
    
    if (!formData.functionName || formData.functionName.trim() === '') {
      errors.push('Function name is required')
    }
    
    if (!formData.returnType) {
      errors.push('Return type is required')
    }
    
    // Validate parameters format
    const params = parseParameters()
    const invalidParams = params.filter(p => !p.name || !p.type)
    if (invalidParams.length > 0) {
      errors.push('Some parameters are missing name or type')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  }

  // FIXED: Safe JSON parsing with better defaults
  const safeJsonParse = (jsonString, defaultValue = null) => {
    try {
      if (!jsonString || jsonString === 'undefined' || jsonString === 'null') {
        return defaultValue
      }
      return JSON.parse(jsonString)
    } catch (error) {
      console.warn('JSON parse error for string:', jsonString)
      return defaultValue
    }
  }

  // NEW: Deep comparison utility for JavaScript
  const deepEqual = (a, b) => {
    if (a === b) return true
    if (typeof a !== 'object' || a === null || typeof b !== 'object' || b === null) {
      return a === b
    }
    
    if (Array.isArray(a) && Array.isArray(b)) {
      if (a.length !== b.length) return false
      for (let i = 0; i < a.length; i++) {
        if (!deepEqual(a[i], b[i])) return false
      }
      return true
    }
    
    const keysA = Object.keys(a)
    const keysB = Object.keys(b)
    if (keysA.length !== keysB.length) return false
    
    for (const key of keysA) {
      if (!keysB.includes(key) || !deepEqual(a[key], b[key])) {
        return false
      }
    }
    return true
  }

  // FIXED: Process test cases with better error handling
  const processTestCases = () => {
    const testCases = formData.testCases || []
    
    return testCases.map(testCase => {
      try {
        let inputData = {}
        let expectedOutput = null
        
        // Handle both new and old formats
        if (testCase.inputData) {
          inputData = safeJsonParse(testCase.inputData, {})
        } else if (testCase.input) {
          try {
            inputData = JSON.parse(testCase.input)
          } catch {
            inputData = { input: testCase.input }
          }
        }
        
        // Parse expected output
        if (testCase.expectedOutput !== undefined) {
          try {
            expectedOutput = JSON.parse(testCase.expectedOutput)
          } catch {
            expectedOutput = testCase.expectedOutput
          }
        }
        
        return {
          ...testCase,
          processedInput: inputData,
          processedOutput: expectedOutput
        }
      } catch (error) {
        console.error('Error processing test case:', error, testCase)
        return {
          ...testCase,
          processedInput: {},
          processedOutput: testCase.expectedOutput
        }
      }
    })
  }

  // Helper functions to format values for each language
  const formatJavaValue = (value) => {
    if (Array.isArray(value)) {
      return `Arrays.asList(${value.map(v => formatJavaValue(v)).join(', ')})`
    } else if (typeof value === 'string') {
      return `"${value.replace(/"/g, '\\"')}"` // Escape quotes
    } else if (typeof value === 'boolean') {
      return value ? 'true' : 'false'
    } else if (value === null) {
      return 'null'
    }
    return String(value)
  }

  const formatPythonValue = (value) => {
    if (Array.isArray(value)) {
      return `[${value.map(v => formatPythonValue(v)).join(', ')}]`
    } else if (typeof value === 'string') {
      return `"${value.replace(/"/g, '\\"')}"` // Escape quotes
    } else if (typeof value === 'boolean') {
      return value ? 'True' : 'False'
    } else if (value === null) {
      return 'None'
    }
    return String(value)
  }

  const formatJavaScriptValue = (value) => {
    if (Array.isArray(value)) {
      return `[${value.map(v => formatJavaScriptValue(v)).join(', ')}]`
    } else if (typeof value === 'string') {
      return `"${value.replace(/"/g, '\\"')}"` // Escape quotes
    } else if (typeof value === 'boolean') {
      return value ? 'true' : 'false'
    } else if (value === null) {
      return 'null'
    } else if (typeof value === 'object') {
      return JSON.stringify(value)
    }
    return String(value)
  }

  const formatCppValue = (value) => {
    if (Array.isArray(value)) {
      return `{${value.map(v => formatCppValue(v)).join(', ')}}`
    } else if (typeof value === 'string') {
      return `"${value.replace(/"/g, '\\"')}"` // Escape quotes
    } else if (typeof value === 'boolean') {
      return value ? 'true' : 'false'
    }
    return String(value)
  }

  const formatCValue = (value) => {
    if (typeof value === 'string') {
      return `"${value}"`
    }
    return String(value)
  }

  // FIXED: Generate test cases with validation
  const generateJavaTestCases = (processedTestCases) => {
    if (processedTestCases.length === 0) {
      return '        System.out.println("No test cases available");'
    }

    let code = `        int passed = 0;
        int total = ${processedTestCases.length};\n\n`

    processedTestCases.forEach((testCase, index) => {
      try {
        const { processedInput, processedOutput } = testCase
        const params = parseParameters()
        
        if (!processedInput || processedOutput === undefined) {
          code += `        // Test ${index + 1} - Invalid test case data\n`
          code += `        System.out.println("✗ Test ${index + 1} SKIPPED - Invalid test data");\n\n`
          return
        }

        // Build function call with actual parameters
        const paramValues = params.map(p => {
          const value = processedInput[p.name]
          if (value === undefined) {
            return `/* missing ${p.name} */ null`
          }
          return formatJavaValue(value)
        }).join(', ')

        code += `        // Test ${index + 1}\n`
        code += `        try {\n`
        code += `            Object result = solution.${formData.functionName || 'solve'}(${paramValues});\n`
        code += `            Object expected = ${formatJavaValue(processedOutput)};\n`
        code += `            if (result.equals(expected)) {\n`
        code += `                passed++;\n`
        code += `                System.out.println("✓ Test ${index + 1} PASSED");\n`
        code += `            } else {\n`
        code += `                System.out.println("✗ Test ${index + 1} FAILED");\n`
        code += `                System.out.println("  Expected: " + expected);\n`
        code += `                System.out.println("  Got: " + result);\n`
        code += `            }\n`
        code += `        } catch (Exception e) {\n`
        code += `            System.out.println("✗ Test ${index + 1} ERROR: " + e.getMessage());\n`
        code += `            e.printStackTrace();\n`
        code += `        }\n\n`
      } catch (error) {
        code += `        // Error in test case ${index + 1}: ${error.message}\n`
        code += `        System.out.println("✗ Test ${index + 1} ERROR: ${error.message}");\n\n`
      }
    })

    code += `        System.out.println("Results: " + passed + "/" + total + " tests passed");\n`
    code += `        if (passed == total) {\n`
    code += `            System.out.println("🎉 All tests passed!");\n`
    code += `        } else {\n`
    code += `            System.out.println("❌ Some tests failed!");\n`
    code += `        }`

    return code
  }

  // FIXED: Generate Python test execution logic
  const generatePythonTestCases = (processedTestCases) => {
    if (processedTestCases.length === 0) {
      return '    print("No test cases available")'
    }

    let code = `    passed = 0\n    total = ${processedTestCases.length}\n\n`

    processedTestCases.forEach((testCase, index) => {
      try {
        const { processedInput, processedOutput } = testCase
        const params = parseParameters()
        
        if (!processedInput || processedOutput === undefined) {
          code += `    # Test ${index + 1} - Invalid test case data\n`
          code += `    print("✗ Test ${index + 1} SKIPPED - Invalid test data")\n`
          code += `    print()\n\n`
          return
        }

        // Build function call with actual parameters
        const paramValues = params.map(p => {
          const value = processedInput[p.name]
          if (value === undefined) {
            return `# missing ${p.name}`
          }
          return formatPythonValue(value)
        }).join(', ')

        code += `    # Test ${index + 1}\n`
        code += `    try:\n`
        code += `        result = solution.${formData.functionName || 'solve'}(${paramValues})\n`
        code += `        expected = ${formatPythonValue(processedOutput)}\n`
        code += `        if result == expected:\n`
        code += `            passed += 1\n`
        code += `            print("✓ Test ${index + 1} PASSED")\n`
        code += `        else:\n`
        code += `            print("✗ Test ${index + 1} FAILED")\n`
        code += `            print(f"  Expected: {expected}")\n`
        code += `            print(f"  Got: {result}")\n`
        code += `    except Exception as e:\n`
        code += `        print(f"✗ Test ${index + 1} ERROR: {e}")\n`
        code += `        import traceback\n`
        code += `        traceback.print_exc()\n`
        code += `    print()\n\n`
      } catch (error) {
        code += `    # Error in test case ${index + 1}: ${error.message}\n`
        code += `    print(f"✗ Test ${index + 1} ERROR: {error.message}")\n`
        code += `    print()\n\n`
      }
    })

    code += `    print(f"Results: {passed}/{total} tests passed")\n`
    code += `    if passed == total:\n`
    code += `        print("🎉 All tests passed!")\n`
    code += `    else:\n`
    code += `        print("❌ Some tests failed!")`

    return code
  }

  // FIXED: Generate JavaScript test execution logic with better comparison
  const generateJavaScriptTestCases = (processedTestCases) => {
    if (processedTestCases.length === 0) {
      return '    console.log("No test cases available");'
    }

    let code = `    let passed = 0;\n    const total = ${processedTestCases.length};\n\n`

    // Add deep equal function for JavaScript
    code += `    function deepEqual(a, b) {\n`
    code += `        if (a === b) return true;\n`
    code += `        if (typeof a !== 'object' || a === null || typeof b !== 'object' || b === null) {\n`
    code += `            return a === b;\n`
    code += `        }\n`
    code += `        if (Array.isArray(a) && Array.isArray(b)) {\n`
    code += `            if (a.length !== b.length) return false;\n`
    code += `            for (let i = 0; i < a.length; i++) {\n`
    code += `                if (!deepEqual(a[i], b[i])) return false;\n`
    code += `            }\n`
    code += `            return true;\n`
    code += `        }\n`
    code += `        const keysA = Object.keys(a);\n`
    code += `        const keysB = Object.keys(b);\n`
    code += `        if (keysA.length !== keysB.length) return false;\n`
    code += `        for (const key of keysA) {\n`
    code += `            if (!keysB.includes(key) || !deepEqual(a[key], b[key])) {\n`
    code += `                return false;\n`
    code += `            }\n`
    code += `        }\n`
    code += `        return true;\n`
    code += `    }\n\n`

    processedTestCases.forEach((testCase, index) => {
      try {
        const { processedInput, processedOutput } = testCase
        const params = parseParameters()
        
        if (!processedInput || processedOutput === undefined) {
          code += `    // Test ${index + 1} - Invalid test case data\n`
          code += `    console.log("✗ Test ${index + 1} SKIPPED - Invalid test data");\n\n`
          return
        }

        // Build function call with actual parameters
        const paramValues = params.map(p => {
          const value = processedInput[p.name]
          if (value === undefined) {
            return `/* missing ${p.name} */`
          }
          return formatJavaScriptValue(value)
        }).join(', ')

        code += `    // Test ${index + 1}\n`
        code += `    try {\n`
        code += `        const result = ${formData.functionName || 'solve'}(${paramValues});\n`
        code += `        const expected = ${formatJavaScriptValue(processedOutput)};\n`
        code += `        if (deepEqual(result, expected)) {\n` // FIXED: Use deepEqual instead of JSON.stringify
        code += `            passed++;\n`
        code += `            console.log("✓ Test ${index + 1} PASSED");\n`
        code += `        } else {\n`
        code += `            console.log("✗ Test ${index + 1} FAILED");\n`
        code += `            console.log("  Expected:", expected);\n`
        code += `            console.log("  Got:", result);\n`
        code += `        }\n`
        code += `    } catch (e) {\n`
        code += `        console.log("✗ Test ${index + 1} ERROR:", e.message);\n`
        code += `        console.log(e.stack);\n`
        code += `    }\n\n`
      } catch (error) {
        code += `    // Error in test case ${index + 1}: ${error.message}\n`
        code += `    console.log("✗ Test ${index + 1} ERROR:", "${error.message}");\n\n`
      }
    })

    code += `    console.log(\`Results: \${passed}/\${total} tests passed\`);\n`
    code += `    if (passed === total) {\n`
    code += `        console.log("🎉 All tests passed!");\n`
    code += `    } else {\n`
    code += `        console.log("❌ Some tests failed!");\n`
    code += `    }`

    return code
  }

  // FIXED: Generate C++ test execution logic
  const generateCppTestCases = (processedTestCases) => {
    if (processedTestCases.length === 0) {
      return '    cout << "No test cases available" << endl;'
    }

    let code = `    int passed = 0;\n    int total = ${processedTestCases.length};\n\n`

    processedTestCases.forEach((testCase, index) => {
      try {
        const { processedInput, processedOutput } = testCase
        const params = parseParameters()
        
        if (!processedInput || processedOutput === undefined) {
          code += `    // Test ${index + 1} - Invalid test case data\n`
          code += `    cout << "✗ Test ${index + 1} SKIPPED - Invalid test data" << endl;\n\n`
          return
        }

        // Build function call with actual parameters
        const paramValues = params.map(p => {
          const value = processedInput[p.name]
          if (value === undefined) {
            return `/* missing ${p.name} */`
          }
          return formatCppValue(value)
        }).join(', ')

        code += `    // Test ${index + 1}\n`
        code += `    try {\n`
        code += `        auto result = solution.${formData.functionName || 'solve'}(${paramValues});\n`
        code += `        auto expected = ${formatCppValue(processedOutput)};\n`
        
        // Handle different comparison for vectors
        if (Array.isArray(processedOutput)) {
          code += `        if (result.size() == expected.size() && std::equal(result.begin(), result.end(), expected.begin())) {\n`
        } else {
          code += `        if (result == expected) {\n`
        }
        
        code += `            passed++;\n`
        code += `            cout << "✓ Test ${index + 1} PASSED" << endl;\n`
        code += `        } else {\n`
        code += `            cout << "✗ Test ${index + 1} FAILED" << endl;\n`
        code += `            cout << "  Expected: " << expected << endl;\n`
        code += `            cout << "  Got: " << result << endl;\n`
        code += `        }\n`
        code += `    } catch (const exception& e) {\n`
        code += `        cout << "✗ Test ${index + 1} ERROR: " << e.what() << endl;\n`
        code += `    }\n\n`
      } catch (error) {
        code += `    // Error in test case ${index + 1}: ${error.message}\n`
        code += `    cout << "✗ Test ${index + 1} ERROR: ${error.message}" << endl;\n\n`
      }
    })

    code += `    cout << "Results: " << passed << "/" << total << " tests passed" << endl;\n`
    code += `    if (passed == total) {\n`
    code += `        cout << "🎉 All tests passed!" << endl;\n`
    code += `    } else {\n`
    code += `        cout << "❌ Some tests failed!" << endl;\n`
    code += `    }`

    return code
  }

  // FIXED: Generate C test execution logic
  const generateCTestCases = (processedTestCases) => {
    if (processedTestCases.length === 0) {
      return '    printf("No test cases available\\n");'
    }

    let code = `    int passed = 0;\n    int total = ${processedTestCases.length};\n\n`

    processedTestCases.forEach((testCase, index) => {
      try {
        const { processedInput, processedOutput } = testCase
        const params = parseParameters()
        
        if (!processedInput || processedOutput === undefined) {
          code += `    // Test ${index + 1} - Invalid test case data\n`
          code += `    printf("✗ Test ${index + 1} SKIPPED - Invalid test data\\n");\n\n`
          return
        }

        // Build function call with actual parameters
        const paramValues = params.map(p => {
          const value = processedInput[p.name]
          if (value === undefined) {
            return `/* missing ${p.name} */`
          }
          return formatCValue(value)
        }).join(', ')

        code += `    // Test ${index + 1}\n`
        code += `    {\n`
        code += `        int result = ${formData.functionName || 'solve'}(${paramValues});\n`
        code += `        int expected = ${formatCValue(processedOutput)};\n`
        code += `        if (result == expected) {\n`
        code += `            passed++;\n`
        code += `            printf("✓ Test ${index + 1} PASSED\\n");\n`
        code += `        } else {\n`
        code += `            printf("✗ Test ${index + 1} FAILED\\n");\n`
        code += `            printf("  Expected: %d\\n", expected);\n`
        code += `            printf("  Got: %d\\n", result);\n`
        code += `        }\n`
        code += `    }\n\n`
      } catch (error) {
        code += `    // Error in test case ${index + 1}: ${error.message}\n`
        code += `    printf("✗ Test ${index + 1} ERROR: ${error.message}\\n");\n\n`
      }
    })

    code += `    printf("Results: %d/%d tests passed\\n", passed, total);\n`
    code += `    if (passed == total) {\n`
    code += `        printf("🎉 All tests passed!\\n");\n`
    code += `    } else {\n`
    code += `        printf("❌ Some tests failed!\\n");\n`
    code += `    }`

    return code
  }

  // FIXED: Generate test cases with validation
  const generateTestCasesPlaceholder = (language) => {
    const processedTestCases = processTestCases()
    
    if (processedTestCases.length === 0) {
      switch (language) {
        case 'JAVA':
          return '        System.out.println("No test cases available");'
        case 'PYTHON':
          return '    print("No test cases available")'
        case 'JAVASCRIPT':
          return '    console.log("No test cases available");'
        case 'CPP':
          return '    cout << "No test cases available" << endl;'
        case 'C':
          return '    printf("No test cases available\\n");'
        default:
          return '        // No test cases available'
      }
    }

    // Generate ACTUAL test execution logic for each language
    switch (language) {
      case 'JAVA':
        return generateJavaTestCases(processedTestCases)
      case 'PYTHON':
        return generatePythonTestCases(processedTestCases)
      case 'JAVASCRIPT':
        return generateJavaScriptTestCases(processedTestCases)
      case 'CPP':
        return generateCppTestCases(processedTestCases)
      case 'C':
        return generateCTestCases(processedTestCases)
      default:
        return `// ${processedTestCases.length} test case(s) defined`
    }
  }

  // FIXED: Main template generation with validation
  const generateTemplate = (language = activeLanguage) => {
    console.log(`Generating template for ${language}...`)
    
    // Validate form data first
    const validation = validateFormData()
    if (!validation.isValid) {
      alert(`Cannot generate template: ${validation.errors.join(', ')}`)
      return
    }
    
    const { functionName = "solve", returnType = "any" } = formData
    
    // Parse parameters
    const params = parseParameters()
    
    // Format parameters - FIXED: Handle Python self parameter correctly
    let formattedParams = formatParams(language, params)
    
    // Map return type
    const mappedReturnType = mapTypeToLanguage(returnType, language)

    // Use the CURRENT templates state
    const currentTemplate = templates[language] || { visibleCode: '', hiddenCode: '' }
    
    // Generate visible code from current template
    let visibleTemplate = currentTemplate.visibleCode
    
    // Replace placeholders - FIXED: Handle JavaScript JSDoc separately
    if (language === 'JAVASCRIPT') {
      // For JavaScript, replace JSDoc parameters first, then function parameters
      visibleTemplate = visibleTemplate
        .replace(/{PARAMETERS}/g, formatJSDocParams(params)) // JSDoc first
        .replace(/{FUNCTION_NAME}/g, functionName)
        .replace(/{RETURN_TYPE}/g, mappedReturnType)
      
      // Then replace the second {PARAMETERS} with function parameters
      const functionParamIndex = visibleTemplate.lastIndexOf('{PARAMETERS}')
      if (functionParamIndex !== -1) {
        visibleTemplate = visibleTemplate.substring(0, functionParamIndex) + 
                         formattedParams + 
                         visibleTemplate.substring(functionParamIndex + 12)
      }
    } else {
      // For other languages, standard replacement
      visibleTemplate = visibleTemplate
        .replace(/{FUNCTION_NAME}/g, functionName)
        .replace(/{RETURN_TYPE}/g, mappedReturnType)
        .replace(/{PARAMETERS}/g, formattedParams)
    }

    // Generate hidden code with test cases
    const testCasesCode = generateTestCasesPlaceholder(language)
    let hiddenTemplate = currentTemplate.hiddenCode
    
    // Better placeholder replacement
    const placeholderPatterns = [
      '// Test cases will be added here',
      '# Test cases will be added here',
      '// Test cases will be auto-generated based on problem test cases',
      '# Test cases will be auto-generated based on problem test cases'
    ]
    
    let replaced = false
    for (const pattern of placeholderPatterns) {
      if (hiddenTemplate.includes(pattern)) {
        hiddenTemplate = hiddenTemplate.replace(pattern, testCasesCode)
        replaced = true
        break
      }
    }
    
    // If no placeholder found, append test cases
    if (!replaced) {
      hiddenTemplate += '\n\n' + testCasesCode
    }

    // Update both templates in one state update
    setTemplates(prevTemplates => {
      const updatedTemplates = {
        ...prevTemplates,
        [language]: {
          visibleCode: visibleTemplate,
          hiddenCode: hiddenTemplate
        }
      }
      
      // Update parent form data
      setFormData(prevFormData => ({
        ...prevFormData,
        codeTemplates: updatedTemplates
      }))
      
      return updatedTemplates
    })

    console.log(`Template generated successfully for ${language}`)
  }

  // Generate all templates
  const generateAllTemplates = () => {
    console.log("Generating all templates...")
    
    // Validate first
    const validation = validateFormData()
    if (!validation.isValid) {
      alert(`Cannot generate templates: ${validation.errors.join(', ')}`)
      return
    }
    
    languages.forEach(lang => {
      generateTemplate(lang.key)
    })
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    alert('Copied to clipboard!')
  }

  const processedTestCases = processTestCases()
  const validation = validateFormData()

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h3 className="text-lg font-semibold text-text">Code Templates</h3>
        <p className="text-sm text-text-muted">
          Configure starter code templates for each supported programming language
        </p>
      </div>

      {/* Validation Errors */}
      {!validation.isValid && (
        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3">
          <h4 className="text-sm font-medium text-red-600 mb-2">Validation Errors</h4>
          <ul className="text-xs text-red-500 list-disc list-inside">
            {validation.errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Language Tabs */}
      <div className="border-b border-border">
        <nav className="flex space-x-8">
          {languages.map((lang) => (
            <button
              key={lang.key}
              onClick={() => setActiveLanguage(lang.key)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeLanguage === lang.key
                  ? "border-primary text-primary"
                  : "border-transparent text-text-muted hover:text-text hover:border-border"
              }`}
            >
              <span className="mr-2">{lang.icon}</span>
              {lang.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Template Editor */}
      <div className="space-y-6">
        {/* Generate Button */}
        <div className="flex justify-between items-center">
          <span className="text-sm text-text-muted">
            Auto-generate template based on function signature and test cases
          </span>
          <div className="space-x-2">
            <button
              onClick={() => generateTemplate(activeLanguage)}
              className="bg-secondary hover:bg-secondary-dark text-white px-4 py-2 rounded-md text-sm"
              disabled={!validation.isValid}
            >
              Generate Current Template
            </button>
            <button
              onClick={generateAllTemplates}
              className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md text-sm"
              disabled={!validation.isValid}
            >
              Generate All Templates
            </button>
          </div>
        </div>

        {/* Debug Info */}
        <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-lg p-3">
          <h4 className="text-sm font-medium text-text mb-2">Debug Info - {activeLanguage}</h4>
          <div className="text-xs text-text-muted space-y-1">
            <p><strong>Function:</strong> {formData.functionName || "solve"}</p>
            <p><strong>Return Type:</strong> {formData.returnType || "any"} → {mapTypeToLanguage(formData.returnType || "any", activeLanguage)}</p>
            <p><strong>Parameters:</strong> {JSON.stringify(parseParameters())}</p>
            <p><strong>Formatted Parameters:</strong> "{formatParams(activeLanguage, parseParameters())}"</p>
            <p><strong>Test Cases:</strong> {processedTestCases.length} defined ({formData.testCases?.length || 0} raw)</p>
            <p><strong>Validation:</strong> {validation.isValid ? '✓ Valid' : '✗ Invalid'}</p>
          </div>
        </div>

        {/* Visible Code Section */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="block text-sm font-medium text-text-muted">
              Visible Code (shown to users) - {activeLanguage}
            </label>
            <div className="space-x-2">
              <button
                onClick={() => generateTemplate(activeLanguage)}
                className="text-xs bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded"
                disabled={!validation.isValid}
              >
                Regenerate
              </button>
              <button
                onClick={() => copyToClipboard(templates[activeLanguage]?.visibleCode || "")}
                className="text-xs text-primary hover:text-primary-dark"
              >
                Copy
              </button>
            </div>
          </div>
          <textarea
            value={templates[activeLanguage]?.visibleCode || ""}
            onChange={(e) => updateTemplate(activeLanguage, "visibleCode", e.target.value)}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono text-sm"
            rows={12}
            placeholder="Enter the visible starter code..."
          />
        </div>

        {/* Hidden Code Section */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="block text-sm font-medium text-text-muted">
              Hidden Code (executed but not shown) - {activeLanguage}
            </label>
            <div className="space-x-2">
              <button
                onClick={() => generateTemplate(activeLanguage)}
                className="text-xs bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded"
                disabled={!validation.isValid}
              >
                Regenerate
              </button>
              <button
                onClick={() => copyToClipboard(templates[activeLanguage]?.hiddenCode || "")}
                className="text-xs text-primary hover:text-primary-dark"
              >
                Copy
              </button>
            </div>
          </div>
          <textarea
            value={templates[activeLanguage]?.hiddenCode || ""}
            onChange={(e) => updateTemplate(activeLanguage, "hiddenCode", e.target.value)}
            className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono text-sm"
            rows={8}
            placeholder="Enter hidden utility code..."
          />
        </div>

        {/* Live Preview */}
        <div className="bg-surface-light rounded-lg p-4 border border-border">
          <h4 className="text-sm font-medium text-text mb-3">Live Preview - {activeLanguage}</h4>
          <div className="bg-surface p-3 rounded border font-mono text-sm text-text">
            <div className="text-green-400 mb-2">// Visible Code Preview ({templates[activeLanguage]?.visibleCode?.length || 0} chars):</div>
            <pre className="whitespace-pre-wrap text-xs bg-surface-dark p-2 rounded mb-4 max-h-40 overflow-y-auto">
              {templates[activeLanguage]?.visibleCode || "No visible code generated"}
            </pre>
            <div className="text-blue-400 mb-2">// Hidden Code Preview ({templates[activeLanguage]?.hiddenCode?.length || 0} chars):</div>
            <pre className="whitespace-pre-wrap text-xs bg-surface-dark p-2 rounded max-h-40 overflow-y-auto">
              {templates[activeLanguage]?.hiddenCode || "No hidden code generated"}
            </pre>
          </div>
        </div>
      </div>
    </div>
  )
}