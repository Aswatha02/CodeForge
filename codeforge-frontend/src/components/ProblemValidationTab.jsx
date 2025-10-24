import { useState } from "react"

export default function ProblemValidationTab({ formData }) {
  const [validationResults, setValidationResults] = useState(null)
  const [isValidating, setIsValidating] = useState(false)

  const validateProblem = async () => {
    setIsValidating(true)
    setValidationResults(null)

    // Simulate validation process
    const results = {
      basic: validateBasicInfo(),
      testCases: validateTestCases(),
      templates: validateTemplates(),
      overall: { isValid: false, errors: [] }
    }

    results.overall.isValid = results.basic.isValid && results.testCases.isValid && results.templates.isValid
    results.overall.errors = [
      ...results.basic.errors,
      ...results.testCases.errors,
      ...results.templates.errors
    ]

    setValidationResults(results)
    setIsValidating(false)
  }

  const validateBasicInfo = () => {
    const errors = []

    if (!formData.title?.trim()) errors.push("Title is required")
    if (!formData.slug?.trim()) errors.push("Slug is required")
    if (!formData.description?.trim()) errors.push("Description is required")
    if (!formData.inputFormat?.trim()) errors.push("Input format is required")
    if (!formData.outputFormat?.trim()) errors.push("Output format is required")
    if (!formData.functionName?.trim()) errors.push("Function name is required")
    if (!formData.returnType?.trim()) errors.push("Return type is required")
    if (!formData.timeLimitMs || formData.timeLimitMs < 100) errors.push("Time limit must be at least 100ms")
    if (!formData.memoryLimitMb || formData.memoryLimitMb < 1) errors.push("Memory limit must be at least 1MB")

    return {
      isValid: errors.length === 0,
      errors
    }
  }

  const validateTestCases = () => {
    const errors = []
    const testCases = formData.testCases || []

    if (testCases.length === 0) {
      errors.push("At least one test case is required")
    } else {
      const sampleCount = testCases.filter(tc => tc.isSample).length
      if (sampleCount === 0) {
        errors.push("At least one sample test case is required")
      }

      testCases.forEach((tc, index) => {
        if (!(tc.inputData || tc.input)?.trim()) errors.push(`Test case ${index + 1}: Input is required`)
        if (!tc.expectedOutput?.trim()) errors.push(`Test case ${index + 1}: Expected output is required`)
      })
    }

    return {
      isValid: errors.length === 0,
      errors
    }
  }

  const validateTemplates = () => {
    const errors = []
    const templates = formData.codeTemplates || {}

    const requiredLanguages = ["JAVA", "PYTHON", "JAVASCRIPT"]

    requiredLanguages.forEach(lang => {
      if (!templates[lang]?.visibleCode?.trim()) {
        errors.push(`${lang}: Visible code template is required`)
      }
    })

    return {
      isValid: errors.length === 0,
      errors
    }
  }

  const previewProblem = () => {
    // This would open a preview modal or navigate to a preview page
    alert("Problem preview functionality would open here")
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h3 className="text-lg font-semibold text-text">Problem Validation & Preview</h3>
        <p className="text-sm text-text-muted">
          Validate your problem configuration and preview how it will appear to users
        </p>
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-4">
        <button
          onClick={validateProblem}
          disabled={isValidating}
          className="bg-primary hover:bg-primary-dark disabled:opacity-50 text-white px-6 py-2 rounded-md"
        >
          {isValidating ? "Validating..." : "Validate Problem"}
        </button>
        <button
          onClick={previewProblem}
          className="bg-secondary hover:bg-secondary-dark text-white px-6 py-2 rounded-md"
        >
          Preview Problem
        </button>
      </div>

      {/* Validation Results */}
      {validationResults && (
        <div className="space-y-4">
          {/* Overall Status */}
          <div className={`p-4 rounded-lg border ${
            validationResults.overall.isValid
              ? "bg-green-500/10 border-green-500/20"
              : "bg-red-500/10 border-red-500/20"
          }`}>
            <div className="flex items-center space-x-2">
              <span className={`text-lg ${
                validationResults.overall.isValid ? "text-green-400" : "text-red-400"
              }`}>
                {validationResults.overall.isValid ? "✅" : "❌"}
              </span>
              <span className="font-medium text-text">
                {validationResults.overall.isValid ? "Problem is valid" : "Problem has validation errors"}
              </span>
            </div>
          </div>

          {/* Detailed Results */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Basic Info Validation */}
            <div className="bg-surface rounded-lg p-4 border border-border">
              <h4 className="font-medium text-text mb-3 flex items-center">
                <span className={`mr-2 ${validationResults.basic.isValid ? "text-green-400" : "text-red-400"}`}>
                  {validationResults.basic.isValid ? "✅" : "❌"}
                </span>
                Basic Information
              </h4>
              {validationResults.basic.errors.length > 0 ? (
                <ul className="space-y-1">
                  {validationResults.basic.errors.map((error, index) => (
                    <li key={index} className="text-sm text-red-400">• {error}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-green-400">All basic information is valid</p>
              )}
            </div>

            {/* Test Cases Validation */}
            <div className="bg-surface rounded-lg p-4 border border-border">
              <h4 className="font-medium text-text mb-3 flex items-center">
                <span className={`mr-2 ${validationResults.testCases.isValid ? "text-green-400" : "text-red-400"}`}>
                  {validationResults.testCases.isValid ? "✅" : "❌"}
                </span>
                Test Cases
              </h4>
              {validationResults.testCases.errors.length > 0 ? (
                <ul className="space-y-1">
                  {validationResults.testCases.errors.map((error, index) => (
                    <li key={index} className="text-sm text-red-400">• {error}</li>
                  ))}
                </ul>
              ) : (
                <div className="text-sm text-green-400">
                  <p>• Test cases are properly configured</p>
                  <p>• Sample test cases are present</p>
                </div>
              )}
            </div>

            {/* Templates Validation */}
            <div className="bg-surface rounded-lg p-4 border border-border">
              <h4 className="font-medium text-text mb-3 flex items-center">
                <span className={`mr-2 ${validationResults.templates.isValid ? "text-green-400" : "text-red-400"}`}>
                  {validationResults.templates.isValid ? "✅" : "❌"}
                </span>
                Code Templates
              </h4>
              {validationResults.templates.errors.length > 0 ? (
                <ul className="space-y-1">
                  {validationResults.templates.errors.map((error, index) => (
                    <li key={index} className="text-sm text-red-400">• {error}</li>
                  ))}
                </ul>
              ) : (
                <div className="text-sm text-green-400">
                  <p>• All language templates are present</p>
                  <p>• Templates have visible code</p>
                </div>
              )}
            </div>
          </div>

          {/* Problem Summary */}
          <div className="bg-surface rounded-lg p-6 border border-border">
            <h4 className="font-medium text-text mb-4">Problem Summary</h4>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <span className="text-text-muted">Title:</span>
                <p className="font-medium text-text">{formData.title || "Not set"}</p>
              </div>
              <div>
                <span className="text-text-muted">Difficulty:</span>
                <p className="font-medium text-text">{formData.difficulty || "Not set"}</p>
              </div>
              <div>
                <span className="text-text-muted">Test Cases:</span>
                <p className="font-medium text-text">{(formData.testCases || []).length}</p>
              </div>
              <div>
                <span className="text-text-muted">Time Limit:</span>
                <p className="font-medium text-text">{formData.timeLimitMs || 0}ms</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Tips */}
      <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4">
        <h4 className="font-medium text-text mb-3">Validation Tips</h4>
        <ul className="space-y-2 text-sm text-text-muted">
          <li>• Ensure all required fields are filled</li>
          <li>• Include at least one sample test case for users</li>
          <li>• Test cases should have valid input/output format</li>
          <li>• Code templates should compile and run correctly</li>
          <li>• Function signatures should match across all languages</li>
        </ul>
      </div>
    </div>
  )
}
