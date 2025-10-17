import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { createProblem, listCategories } from "../api";

export default function CreateProblem() {
  const [formData, setFormData] = useState({
    title: "",
    slug: "",
    description: "",
    difficulty: "EASY",
    categoryId: "",
    inputFormat: "",
    outputFormat: "",
    constraints: "",
    timeLimitMs: 2000,
    memoryLimitMb: 256
  });
  
  const [codeTemplates, setCodeTemplates] = useState([
    { 
      language: "JAVASCRIPT", 
      templateCode: `function solve(input) {\n    // Write your solution here\n    // Example: For twoSum problem\n    // const { nums, target } = input;\n    // Your logic here\n    \n}` 
    },
    { 
      language: "PYTHON", 
      templateCode: `class Solution:\n    def solve(self, input):\n        # Write your solution here\n        # Example: For twoSum problem\n        # nums = input['nums']\n        # target = input['target']\n        # Your logic here\n        ` 
    },
    { 
      language: "JAVA", 
      templateCode: `class Solution {\n    public int[] solve(int[] nums, int target) {\n        // Write your solution here\n        // Your logic here\n        \n    }\n}` 
    },
    { 
      language: "CPP", 
      templateCode: `class Solution {\npublic:\n    vector<int> solve(vector<int>& nums, int target) {\n        // Write your solution here\n        // Your logic here\n        \n    }\n};` 
    }
  ]);
  
  const [testCases, setTestCases] = useState([
    { 
      inputData: '{"nums": [2,7,11,15], "target": 9}', 
      expectedOutput: "[0,1]", 
      isSample: true,
      testCaseName: "Example 1",
      explanation: "nums[0] + nums[1] = 2 + 7 = 9"
    }
  ]);
  
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const { user, isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAdmin) {
      navigate("/dashboard");
      return;
    }
    loadCategories();
  }, [isAdmin, navigate]);

  const loadCategories = async () => {
    try {
      const data = await listCategories();
      setCategories(data);
      if (data.length > 0 && !formData.categoryId) {
        setFormData(prev => ({ ...prev, categoryId: data[0].id }));
      }
    } catch (err) {
      console.error('Error loading categories:', err);
      setError("Failed to load categories");
    }
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Auto-generate slug from title
    if (field === 'title') {
      const generatedSlug = value.toLowerCase()
        .replace(/\s+/g, '-')
        .replace(/[^a-z0-9-]/g, '');
      setFormData(prev => ({ ...prev, slug: generatedSlug }));
    }
  };

  const handleAddTestCase = () => {
    setTestCases(prev => [...prev, { 
      inputData: "", 
      expectedOutput: "", 
      isSample: false,
      testCaseName: `Test Case ${prev.length + 1}`,
      explanation: ""
    }]);
  };

  const handleRemoveTestCase = (index) => {
    if (testCases.length > 1) {
      setTestCases(prev => prev.filter((_, i) => i !== index));
    }
  };

  const handleTestCaseChange = (index, field, value) => {
    setTestCases(prev => prev.map((testCase, i) => 
      i === index ? { ...testCase, [field]: value } : testCase
    ));
  };

  const handleCodeTemplateChange = (language, newTemplate) => {
    setCodeTemplates(prev => prev.map(template => 
      template.language === language 
        ? { ...template, templateCode: newTemplate }
        : template
    ));
  };

  // Enhanced test case validation
  const validateTestCase = (testCase) => {
    try {
      // Validate input is proper JSON
      const inputObj = JSON.parse(testCase.inputData);
      
      // Validate expected output format
      JSON.parse(testCase.expectedOutput);
      
      // Basic validation for common problem types
      if (testCase.inputData.includes('nums') && testCase.inputData.includes('target')) {
        if (!Array.isArray(inputObj.nums) || typeof inputObj.target !== 'number') {
          return { isValid: false, error: "Input must contain 'nums' array and 'target' number" };
        }
      }
      
      return { isValid: true };
    } catch (error) {
      return { 
        isValid: false, 
        error: `Invalid JSON format: ${error.message}` 
      };
    }
  };

  // Validate all form data
  const validateForm = () => {
    if (!formData.title.trim()) {
      return "Problem title is required";
    }
    if (!formData.slug.trim()) {
      return "Problem slug is required";
    }
    if (!formData.description.trim()) {
      return "Problem description is required";
    }
    if (!formData.inputFormat.trim()) {
      return "Input format is required";
    }
    if (!formData.outputFormat.trim()) {
      return "Output format is required";
    }
    if (!formData.categoryId) {
      return "Category is required";
    }

    // Validate test cases
    const validTestCases = testCases.filter(tc => 
      tc.inputData.trim() && tc.expectedOutput.trim()
    );

    if (validTestCases.length === 0) {
      return "Please add at least one test case";
    }

    // Validate each test case
    for (let i = 0; i < validTestCases.length; i++) {
      const validation = validateTestCase(validTestCases[i]);
      if (!validation.isValid) {
        return `Test Case ${i + 1}: ${validation.error}`;
      }
    }

    // Validate code templates
    const validCodeTemplates = codeTemplates.filter(ct => ct.templateCode.trim());
    if (validCodeTemplates.length === 0) {
      return "Please provide at least one code template";
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      // Validate form
      const validationError = validateForm();
      if (validationError) {
        setError(validationError);
        setLoading(false);
        return;
      }

      // Filter valid test cases
      const validTestCases = testCases.filter(tc => 
        tc.inputData.trim() && tc.expectedOutput.trim()
      );

      // Filter valid code templates
      const validCodeTemplates = codeTemplates.filter(ct => ct.templateCode.trim());

      // Prepare problem data
      const problemData = {
        title: formData.title.trim(),
        slug: formData.slug.toLowerCase().replace(/\s+/g, '-'),
        description: formData.description.trim(),
        difficulty: formData.difficulty,
        inputFormat: formData.inputFormat.trim(),
        outputFormat: formData.outputFormat.trim(),
        constraints: formData.constraints.trim(),
        timeLimitMs: formData.timeLimitMs,
        memoryLimitMb: formData.memoryLimitMb,
        categoryIds: [parseInt(formData.categoryId)],
        testCases: validTestCases.map(tc => ({
          inputData: tc.inputData,
          expectedOutput: tc.expectedOutput,
          isSample: tc.isSample,
          testCaseName: tc.testCaseName || `Test Case ${Date.now()}`,
          explanation: tc.explanation || ""
        })),
        codeTemplates: validCodeTemplates.map(ct => ({
          language: ct.language,
          templateCode: ct.templateCode
        }))
      };

      console.log('Creating problem with data:', problemData);
      
      await createProblem(problemData, user.id);
      
      setSuccess("Problem created successfully!");
      setTimeout(() => {
        navigate("/problems");
      }, 1500);
      
    } catch (err) {
      console.error('Error creating problem:', err);
      setError(err?.response?.data?.message || err?.data?.message || "Failed to create problem. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const addSampleTestCase = () => {
    setTestCases(prev => [...prev, { 
      inputData: '{"nums": [3,2,4], "target": 6}', 
      expectedOutput: "[1,2]", 
      isSample: true,
      testCaseName: "Example 2",
      explanation: "nums[1] + nums[2] = 2 + 4 = 6"
    }]);
  };

  const addHiddenTestCase = () => {
    setTestCases(prev => [...prev, { 
      inputData: '{"nums": [3,3], "target": 6}', 
      expectedOutput: "[0,1]", 
      isSample: false,
      testCaseName: "Hidden Case 1",
      explanation: "Same elements should work correctly"
    }]);
  };

  if (!isAdmin) {
    return null;
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ maxWidth: '900px', margin: '0 auto' }}>
        <div style={{ marginBottom: '32px' }}>
          <h1 style={{ 
            fontSize: '32px', 
            fontWeight: '700', 
            marginBottom: '8px',
            color: '#333'
          }}>
            Create Problem
          </h1>
          <p style={{ color: '#666', fontSize: '16px' }}>
            Add a new coding problem to the platform
          </p>
        </div>

        {error && (
          <div style={{
            padding: '12px 16px',
            background: '#f8d7da',
            color: '#721c24',
            border: '1px solid #f5c6cb',
            borderRadius: '6px',
            marginBottom: '24px'
          }}>
            {error}
          </div>
        )}

        {success && (
          <div style={{
            padding: '12px 16px',
            background: '#d1edff',
            color: '#0c5460',
            border: '1px solid #bee5eb',
            borderRadius: '6px',
            marginBottom: '24px'
          }}>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ background: 'white', padding: '32px', borderRadius: '8px', border: '1px solid #e8e8e8' }}>
          <div style={{ display: 'grid', gap: '24px' }}>
            
            {/* Problem Basic Info */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Problem Title *
                </label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => handleInputChange('title', e.target.value)}
                  placeholder="e.g., Two Sum"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px'
                  }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Problem Slug *
                </label>
                <input
                  type="text"
                  value={formData.slug}
                  onChange={(e) => handleInputChange('slug', e.target.value)}
                  placeholder="e.g., two-sum"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px'
                  }}
                  required
                />
                <small style={{ color: '#666', marginTop: '4px', display: 'block' }}>
                  URL-friendly identifier
                </small>
              </div>
            </div>

            {/* Description */}
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                Problem Description *
              </label>
              <textarea
                value={formData.description}
                onChange={(e) => handleInputChange('description', e.target.value)}
                placeholder="Describe the problem statement, examples, and approach..."
                rows="6"
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '6px',
                  fontSize: '16px',
                  resize: 'vertical',
                  fontFamily: 'inherit'
                }}
                required
              />
            </div>

            {/* Input/Output Format */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Input Format *
                </label>
                <textarea
                  value={formData.inputFormat}
                  onChange={(e) => handleInputChange('inputFormat', e.target.value)}
                  placeholder="Describe the input format and structure"
                  rows="4"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px',
                    resize: 'vertical',
                    fontFamily: 'inherit'
                  }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Output Format *
                </label>
                <textarea
                  value={formData.outputFormat}
                  onChange={(e) => handleInputChange('outputFormat', e.target.value)}
                  placeholder="Describe the expected output format"
                  rows="4"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px',
                    resize: 'vertical',
                    fontFamily: 'inherit'
                  }}
                  required
                />
              </div>
            </div>

            {/* Constraints and Limits */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Constraints
                </label>
                <textarea
                  value={formData.constraints}
                  onChange={(e) => handleInputChange('constraints', e.target.value)}
                  placeholder="List the constraints..."
                  rows="3"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px',
                    resize: 'vertical',
                    fontFamily: 'inherit'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Time Limit (ms) *
                </label>
                <input
                  type="number"
                  value={formData.timeLimitMs}
                  onChange={(e) => handleInputChange('timeLimitMs', parseInt(e.target.value) || 2000)}
                  min="100"
                  max="10000"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px'
                  }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Memory Limit (MB) *
                </label>
                <input
                  type="number"
                  value={formData.memoryLimitMb}
                  onChange={(e) => handleInputChange('memoryLimitMb', parseInt(e.target.value) || 256)}
                  min="64"
                  max="1024"
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px'
                  }}
                  required
                />
              </div>
            </div>

            {/* Difficulty and Category */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Difficulty *
                </label>
                <select
                  value={formData.difficulty}
                  onChange={(e) => handleInputChange('difficulty', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px',
                    background: 'white'
                  }}
                >
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Category *
                </label>
                <select
                  value={formData.categoryId}
                  onChange={(e) => handleInputChange('categoryId', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '6px',
                    fontSize: '16px',
                    background: 'white'
                  }}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map(category => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Code Templates */}
            <div>
              <label style={{ display: 'block', marginBottom: '16px', fontWeight: '600', color: '#333' }}>
                Code Templates *
              </label>
              
              {codeTemplates.map((codeTemplate) => (
                <div key={codeTemplate.language} style={{ 
                  border: '1px solid #e8e8e8', 
                  borderRadius: '6px', 
                  padding: '16px', 
                  marginBottom: '16px',
                  background: '#fafafa'
                }}>
                  <h4 style={{ margin: '0 0 12px 0', color: '#333' }}>
                    {codeTemplate.language}
                  </h4>
                  <textarea
                    value={codeTemplate.templateCode}
                    onChange={(e) => handleCodeTemplateChange(codeTemplate.language, e.target.value)}
                    placeholder={`Write the ${codeTemplate.language} code template...`}
                    rows="8"
                    style={{
                      width: '100%',
                      padding: '12px',
                      border: '1px solid #ddd',
                      borderRadius: '4px',
                      fontSize: '14px',
                      resize: 'vertical',
                      fontFamily: 'monospace',
                      lineHeight: '1.4'
                    }}
                  />
                  <small style={{ color: '#666', marginTop: '4px', display: 'block' }}>
                    Use a function named 'solve' that takes input parameters. The input will be automatically parsed from JSON.
                  </small>
                </div>
              ))}
            </div>

            {/* Test Cases */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <label style={{ fontWeight: '600', color: '#333' }}>
                  Test Cases *
                </label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    type="button"
                    onClick={addSampleTestCase}
                    style={{
                      padding: '8px 12px',
                      background: '#17a2b8',
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    + Add Sample
                  </button>
                  <button
                    type="button"
                    onClick={addHiddenTestCase}
                    style={{
                      padding: '8px 12px',
                      background: '#6c757d',
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    + Add Hidden
                  </button>
                  <button
                    type="button"
                    onClick={handleAddTestCase}
                    style={{
                      padding: '8px 12px',
                      background: '#28a745',
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    + Add Custom
                  </button>
                </div>
              </div>

              {testCases.map((testCase, index) => (
                <div key={index} style={{ 
                  border: '1px solid #e8e8e8', 
                  borderRadius: '6px', 
                  padding: '16px', 
                  marginBottom: '16px',
                  background: testCase.isSample ? '#f8f9fa' : '#fff'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <h4 style={{ margin: 0, color: '#333' }}>
                        {testCase.testCaseName || `Test Case ${index + 1}`}
                      </h4>
                      {testCase.isSample && (
                        <span style={{
                          padding: '2px 8px',
                          background: '#17a2b8',
                          color: 'white',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: '500'
                        }}>
                          Sample
                        </span>
                      )}
                    </div>
                    {testCases.length > 1 && (
                      <button
                        type="button"
                        onClick={() => handleRemoveTestCase(index)}
                        style={{
                          padding: '4px 8px',
                          background: '#dc3545',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '12px'
                        }}
                      >
                        Remove
                      </button>
                    )}
                  </div>

                  <div style={{ marginBottom: '12px' }}>
                    <input
                      type="text"
                      value={testCase.testCaseName}
                      onChange={(e) => handleTestCaseChange(index, 'testCaseName', e.target.value)}
                      placeholder="Test case name"
                      style={{
                        width: '100%',
                        padding: '8px',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        fontSize: '14px',
                        marginBottom: '8px'
                      }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '4px', fontSize: '14px', color: '#666' }}>
                        Input (JSON format) *
                      </label>
                      <textarea
                        value={testCase.inputData}
                        onChange={(e) => handleTestCaseChange(index, 'inputData', e.target.value)}
                        placeholder='{"nums": [2,7,11,15], "target": 9}'
                        rows="4"
                        style={{
                          width: '100%',
                          padding: '8px',
                          border: '1px solid #ddd',
                          borderRadius: '4px',
                          fontSize: '14px',
                          resize: 'vertical',
                          fontFamily: 'monospace'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '4px', fontSize: '14px', color: '#666' }}>
                        Expected Output (JSON format) *
                      </label>
                      <textarea
                        value={testCase.expectedOutput}
                        onChange={(e) => handleTestCaseChange(index, 'expectedOutput', e.target.value)}
                        placeholder="[0,1]"
                        rows="4"
                        style={{
                          width: '100%',
                          padding: '8px',
                          border: '1px solid #ddd',
                          borderRadius: '4px',
                          fontSize: '14px',
                          resize: 'vertical',
                          fontFamily: 'monospace'
                        }}
                      />
                    </div>
                  </div>

                  <div style={{ marginBottom: '12px' }}>
                    <label style={{ display: 'block', marginBottom: '4px', fontSize: '14px', color: '#666' }}>
                      Explanation (Optional)
                    </label>
                    <textarea
                      value={testCase.explanation}
                      onChange={(e) => handleTestCaseChange(index, 'explanation', e.target.value)}
                      placeholder="Explain what this test case validates..."
                      rows="2"
                      style={{
                        width: '100%',
                        padding: '8px',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        fontSize: '14px',
                        resize: 'vertical'
                      }}
                    />
                  </div>

                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={testCase.isSample}
                      onChange={(e) => handleTestCaseChange(index, 'isSample', e.target.checked)}
                    />
                    <span style={{ fontSize: '14px', color: '#666' }}>
                      Show as sample test case to users
                    </span>
                  </label>
                </div>
              ))}
            </div>

            {/* Submit Button */}
            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', paddingTop: '16px', borderTop: '1px solid #e8e8e8' }}>
              <button
                type="button"
                onClick={() => navigate("/problems")}
                style={{
                  padding: '12px 24px',
                  background: 'transparent',
                  color: '#666',
                  border: '1px solid #ddd',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontSize: '16px'
                }}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                style={{
                  padding: '12px 24px',
                  background: loading ? '#6c757d' : '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  fontSize: '16px',
                  minWidth: '120px'
                }}
              >
                {loading ? (
                  <span style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <div style={{ 
                      width: '16px', 
                      height: '16px', 
                      border: '2px solid transparent',
                      borderTop: '2px solid white',
                      borderRadius: '50%',
                      animation: 'spin 1s linear infinite',
                      marginRight: '8px'
                    }}></div>
                    Creating...
                  </span>
                ) : (
                  'Create Problem'
                )}
              </button>
            </div>
          </div>
        </form>
      </div>

      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}