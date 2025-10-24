import { useState, useEffect } from "react"

export default function TestCasesTab({ formData, setFormData }) {
  const [testCases, setTestCases] = useState(formData.testCases || [])
  const [selectedTestCases, setSelectedTestCases] = useState([])
  const [showAddModal, setShowAddModal] = useState(false)
  const [editingTestCase, setEditingTestCase] = useState(null)
  const [newTestCase, setNewTestCase] = useState({
    inputData: "{}", // Changed from input to inputData (JSON)
    expectedOutput: "", // Keep as string for flexibility
    isSample: false,
    explanation: ""
  })
  const [inputError, setInputError] = useState("")

  useEffect(() => {
    setTestCases(formData.testCases || [])
  }, [formData.testCases])

  // Parse function parameters to guide test case creation
  const parseParameters = () => {
    try {
      if (formData.parameters) {
        if (typeof formData.parameters === 'string' && formData.parameters.trim()) {
          return JSON.parse(formData.parameters)
        } else if (Array.isArray(formData.parameters)) {
          return formData.parameters
        }
      }
    } catch (error) {
      // Silently handle parsing errors to avoid console spam
    }
    return []
  }

  const validateJsonInput = (jsonString) => {
    try {
      if (!jsonString.trim()) return { isValid: false, error: "Input cannot be empty" }
      
      const parsed = JSON.parse(jsonString)
      if (typeof parsed !== 'object' || parsed === null) {
        return { isValid: false, error: "Input must be a JSON object" }
      }
      
      return { isValid: true, data: parsed }
    } catch (error) {
      return { isValid: false, error: "Invalid JSON format" }
    }
  }

  const validateExpectedOutput = (output) => {
    try {
      if (!output.trim()) return { isValid: false, error: "Expected output cannot be empty" }
      
      // Try to parse as JSON, but also allow simple values
      try {
        JSON.parse(output)
        return { isValid: true }
      } catch {
        // If it's not JSON, it might be a simple value - that's okay
        return { isValid: true }
      }
    } catch (error) {
      return { isValid: false, error: "Invalid output format" }
    }
  }

  const handleAddTestCase = () => {
    // Validate input data
    const inputValidation = validateJsonInput(newTestCase.inputData)
    if (!inputValidation.isValid) {
      setInputError(inputValidation.error)
      return
    }

    // Validate expected output
    const outputValidation = validateExpectedOutput(newTestCase.expectedOutput)
    if (!outputValidation.isValid) {
      setInputError(outputValidation.error)
      return
    }

    const updatedTestCases = [...testCases, { 
      ...newTestCase, 
      id: Date.now(),
      // Ensure we store valid JSON for inputData
      inputData: inputValidation.data ? JSON.stringify(inputValidation.data) : newTestCase.inputData
    }]
    
    setTestCases(updatedTestCases)
    setFormData({ ...formData, testCases: updatedTestCases })
    setNewTestCase({ inputData: "{}", expectedOutput: "", isSample: false, explanation: "" })
    setInputError("")
    setShowAddModal(false)
  }

  const handleEditTestCase = (testCase) => {
    setEditingTestCase(testCase)
    
    // Parse existing inputData for editing
    let inputData = "{}"
    try {
      if (testCase.inputData) {
        const parsed = JSON.parse(testCase.inputData)
        inputData = JSON.stringify(parsed, null, 2)
      }
    } catch {
      inputData = testCase.inputData || "{}"
    }
    
    setNewTestCase({
      inputData: inputData,
      expectedOutput: testCase.expectedOutput || "",
      isSample: testCase.isSample || false,
      explanation: testCase.explanation || ""
    })
    setInputError("")
  }

  const handleSaveEdit = () => {
    // Validate input data
    const inputValidation = validateJsonInput(newTestCase.inputData)
    if (!inputValidation.isValid) {
      setInputError(inputValidation.error)
      return
    }

    // Validate expected output
    const outputValidation = validateExpectedOutput(newTestCase.expectedOutput)
    if (!outputValidation.isValid) {
      setInputError(outputValidation.error)
      return
    }

    const updatedTestCases = testCases.map(tc =>
      tc.id === editingTestCase.id ? { 
        ...tc, 
        ...newTestCase,
        inputData: inputValidation.data ? JSON.stringify(inputValidation.data) : newTestCase.inputData
      } : tc
    )
    
    setTestCases(updatedTestCases)
    setFormData({ ...formData, testCases: updatedTestCases })
    setEditingTestCase(null)
    setNewTestCase({ inputData: "{}", expectedOutput: "", isSample: false, explanation: "" })
    setInputError("")
  }

  const handleDeleteTestCase = (testCaseId) => {
    if (!confirm("Are you sure you want to delete this test case?")) return

    const updatedTestCases = testCases.filter(tc => tc.id !== testCaseId)
    setTestCases(updatedTestCases)
    setFormData({ ...formData, testCases: updatedTestCases })
  }

  const handleBulkDelete = () => {
    if (selectedTestCases.length === 0) return
    if (!confirm(`Are you sure you want to delete ${selectedTestCases.length} test case(s)?`)) return

    const updatedTestCases = testCases.filter(tc => !selectedTestCases.includes(tc.id))
    setTestCases(updatedTestCases)
    setFormData({ ...formData, testCases: updatedTestCases })
    setSelectedTestCases([])
  }

  const handleBulkVisibilityChange = (isSample) => {
    if (selectedTestCases.length === 0) return

    const updatedTestCases = testCases.map(tc =>
      selectedTestCases.includes(tc.id) ? { ...tc, isSample } : tc
    )
    setTestCases(updatedTestCases)
    setFormData({ ...formData, testCases: updatedTestCases })
  }

  const toggleTestCaseSelection = (testCaseId) => {
    setSelectedTestCases(prev =>
      prev.includes(testCaseId)
        ? prev.filter(id => id !== testCaseId)
        : [...prev, testCaseId]
    )
  }

  const selectAllTestCases = () => {
    setSelectedTestCases(testCases.map(tc => tc.id))
  }

  const clearSelection = () => {
    setSelectedTestCases([])
  }

  const formatTestCaseDisplay = (testCase) => {
    try {
      const inputData = JSON.parse(testCase.inputData)
      return Object.entries(inputData).map(([key, value]) => 
        `${key}: ${JSON.stringify(value)}`
      ).join(', ')
    } catch {
      return testCase.inputData
    }
  }

  const parameters = parseParameters()
  const hasParameters = parameters.length > 0

  return (
    <div className="space-y-6">
      {/* Header with Actions */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold text-text">Test Cases</h3>
          <p className="text-sm text-text-muted">Manage test cases for problem validation</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md text-sm"
          >
            Add Test Case
          </button>
        </div>
      </div>

      {/* Parameters Guide */}
      {hasParameters && (
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4">
          <h4 className="text-sm font-medium text-text mb-2">Function Parameters Guide</h4>
          <div className="text-xs text-text-muted space-y-1">
            <p>Your function has these parameters. Use JSON format in test cases:</p>
            <div className="font-mono bg-surface-light p-2 rounded mt-2">
              {parameters.map((param, index) => (
                <div key={index}>
                  {param.name}: {param.type}
                </div>
              ))}
            </div>
            <p className="mt-2 text-yellow-600">
              Example: {"{"}"{parameters[0]?.name}": value, "{parameters[1]?.name}": value{"}"}
            </p>
          </div>
        </div>
      )}

      {/* Bulk Actions */}
      {selectedTestCases.length > 0 && (
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-text">
              {selectedTestCases.length} test case(s) selected
            </span>
            <div className="flex space-x-2">
              <button
                onClick={() => handleBulkVisibilityChange(true)}
                className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm"
              >
                Make Sample
              </button>
              <button
                onClick={() => handleBulkVisibilityChange(false)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-1 rounded text-sm"
              >
                Make Hidden
              </button>
              <button
                onClick={handleBulkDelete}
                className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm"
              >
                Delete Selected
              </button>
              <button
                onClick={clearSelection}
                className="bg-gray-500 hover:bg-gray-600 text-white px-3 py-1 rounded text-sm"
              >
                Clear Selection
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Test Cases List */}
      <div className="space-y-4">
        {testCases.length === 0 ? (
          <div className="text-center py-8 text-text-muted">
            No test cases added yet. Click "Add Test Case" to get started.
          </div>
        ) : (
          <>
            {/* Select All */}
            <div className="flex items-center space-x-3 pb-2 border-b border-border">
              <input
                type="checkbox"
                checked={selectedTestCases.length === testCases.length && testCases.length > 0}
                onChange={selectedTestCases.length === testCases.length ? clearSelection : selectAllTestCases}
                className="rounded"
              />
              <span className="text-sm font-medium text-text-muted">Select All</span>
              <span className="text-sm text-text-muted ml-auto">
                {testCases.length} test case(s)
              </span>
            </div>

            {/* Test Cases */}
            {testCases.map((testCase, index) => (
              <div key={testCase.id} className="border border-border rounded-lg p-4">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3 flex-1">
                    <input
                      type="checkbox"
                      checked={selectedTestCases.includes(testCase.id)}
                      onChange={() => toggleTestCaseSelection(testCase.id)}
                      className="mt-1 rounded"
                    />
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <span className="text-sm font-medium text-text">Test Case #{index + 1}</span>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          testCase.isSample
                            ? 'bg-green-500/20 text-green-400'
                            : 'bg-gray-500/20 text-gray-400'
                        }`}>
                          {testCase.isSample ? 'Sample' : 'Hidden'}
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-xs font-medium text-text-muted mb-1">Input</label>
                          <div className="bg-surface-light p-2 rounded text-sm font-mono text-text max-h-20 overflow-y-auto">
                            {formatTestCaseDisplay(testCase)}
                          </div>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-text-muted mb-1">Expected Output</label>
                          <div className="bg-surface-light p-2 rounded text-sm font-mono text-text max-h-20 overflow-y-auto">
                            {testCase.expectedOutput}
                          </div>
                        </div>
                      </div>

                      {testCase.explanation && (
                        <div className="mt-2">
                          <label className="block text-xs font-medium text-text-muted mb-1">Explanation</label>
                          <div className="bg-surface-light p-2 rounded text-sm text-text">
                            {testCase.explanation}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex space-x-2 ml-4">
                    <button
                      onClick={() => handleEditTestCase(testCase)}
                      className="text-blue-400 hover:text-blue-300 text-sm px-2 py-1 rounded"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDeleteTestCase(testCase.id)}
                      className="text-red-400 hover:text-red-300 text-sm px-2 py-1 rounded"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </>
        )}
      </div>

      {/* Add Test Case Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-surface rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold text-text mb-4">Add Test Case</h3>
            
            {inputError && (
              <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3 mb-4">
                <p className="text-red-400 text-sm">{inputError}</p>
              </div>
            )}

            {hasParameters && (
              <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-3 mb-4">
                <h4 className="text-sm font-medium text-text mb-2">Parameter Guide</h4>
                <div className="text-xs text-text-muted font-mono space-y-1">
                  {parameters.map((param, index) => (
                    <div key={index}>{param.name} ({param.type})</div>
                  ))}
                </div>
              </div>
            )}

            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-muted mb-2">
                    Input Data (JSON) *
                  </label>
                  <textarea
                    value={newTestCase.inputData}
                    onChange={(e) => {
                      setNewTestCase({ ...newTestCase, inputData: e.target.value })
                      setInputError("")
                    }}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono"
                    rows="8"
                    placeholder={`Enter input as JSON object:
{
  "param1": value1,
  "param2": value2
}`}
                  />
                  <p className="text-xs text-text-muted mt-1">
                    Use JSON format with parameter names as keys
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-text-muted mb-2">
                    Expected Output *
                  </label>
                  <textarea
                    value={newTestCase.expectedOutput}
                    onChange={(e) => {
                      setNewTestCase({ ...newTestCase, expectedOutput: e.target.value })
                      setInputError("")
                    }}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono"
                    rows="8"
                    placeholder={`Enter expected output:
"result" or 42 or [1, 2, 3] or {"key": "value"}`}
                  />
                  <p className="text-xs text-text-muted mt-1">
                    Can be string, number, array, object, or simple value
                  </p>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text-muted mb-2">Explanation (Optional)</label>
                <textarea
                  value={newTestCase.explanation}
                  onChange={(e) => setNewTestCase({ ...newTestCase, explanation: e.target.value })}
                  className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
                  rows="3"
                  placeholder="Explain this test case..."
                />
              </div>

              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  checked={newTestCase.isSample}
                  onChange={(e) => setNewTestCase({ ...newTestCase, isSample: e.target.checked })}
                  className="rounded"
                />
                <label className="text-sm text-text-muted">Show as sample test case to users</label>
              </div>
            </div>
            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => {
                  setShowAddModal(false)
                  setInputError("")
                }}
                className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
              >
                Cancel
              </button>
              <button
                onClick={handleAddTestCase}
                className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
              >
                Add Test Case
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Test Case Modal */}
      {editingTestCase && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-surface rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold text-text mb-4">Edit Test Case</h3>
            
            {inputError && (
              <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3 mb-4">
                <p className="text-red-400 text-sm">{inputError}</p>
              </div>
            )}

            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-muted mb-2">
                    Input Data (JSON) *
                  </label>
                  <textarea
                    value={newTestCase.inputData}
                    onChange={(e) => {
                      setNewTestCase({ ...newTestCase, inputData: e.target.value })
                      setInputError("")
                    }}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono"
                    rows="8"
                    placeholder={`Enter input as JSON object:
{
  "param1": value1,
  "param2": value2
}`}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text-muted mb-2">
                    Expected Output *
                  </label>
                  <textarea
                    value={newTestCase.expectedOutput}
                    onChange={(e) => {
                      setNewTestCase({ ...newTestCase, expectedOutput: e.target.value })
                      setInputError("")
                    }}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none font-mono"
                    rows="8"
                    placeholder="Enter expected output..."
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text-muted mb-2">Explanation (Optional)</label>
                <textarea
                  value={newTestCase.explanation}
                  onChange={(e) => setNewTestCase({ ...newTestCase, explanation: e.target.value })}
                  className="w-full px-3 py-2 rounded-md bg-surface-light border border-border focus:border-primary focus:outline-none"
                  rows="3"
                  placeholder="Explain this test case..."
                />
              </div>

              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  checked={newTestCase.isSample}
                  onChange={(e) => setNewTestCase({ ...newTestCase, isSample: e.target.checked })}
                  className="rounded"
                />
                <label className="text-sm text-text-muted">Show as sample test case to users</label>
              </div>
            </div>
            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => {
                  setEditingTestCase(null)
                  setInputError("")
                }}
                className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveEdit}
                className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}