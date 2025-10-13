import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { createProblem, listCategories } from "../api";

export default function CreateProblem() {
  const [title, setTitle] = useState("");
  const [slug, setSlug] = useState("");
  const [description, setDescription] = useState("");
  const [difficulty, setDifficulty] = useState("EASY");
  const [categoryId, setCategoryId] = useState("");
  const [testCases, setTestCases] = useState([{ input: "", expectedOutput: "", isSample: false }]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
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
      console.log('Loaded categories:', data);
      setCategories(data);
      if (data.length > 0) {
        setCategoryId(data[0].id);
      }
    } catch (err) {
      console.error('Error loading categories:', err);
      setError("Failed to load categories");
    }
  };

  const handleAddTestCase = () => {
    setTestCases([...testCases, { input: "", expectedOutput: "", isSample: false }]);
  };

  const handleRemoveTestCase = (index) => {
    if (testCases.length > 1) {
      const newTestCases = testCases.filter((_, i) => i !== index);
      setTestCases(newTestCases);
    }
  };

  const handleTestCaseChange = (index, field, value) => {
    const newTestCases = testCases.map((testCase, i) => {
      if (i === index) {
        return { ...testCase, [field]: value };
      }
      return testCase;
    });
    setTestCases(newTestCases);
  };
  const handleSubmit = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError("");

  try {
    // Validate required fields
    if (!title || !slug || !description || !categoryId) {
      setError("Please fill in all required fields");
      setLoading(false);
      return;
    }

    // Validate test cases
    const validTestCases = testCases.filter(tc => tc.input.trim() && tc.expectedOutput.trim());
    if (validTestCases.length === 0) {
      setError("Please add at least one test case");
      setLoading(false);
      return;
    }

    // FIXED: Send categories as objects with only ID
    const problemData = {
      title,
      slug: slug.toLowerCase().replace(/\s+/g, '-'),
      description,
      difficulty,
      categories: [
        {
          id: parseInt(categoryId)
        }
      ],
      testCases: validTestCases
    };

    console.log('Creating problem with data:', problemData);
    console.log('Creator ID:', user.id);
    
    await createProblem(problemData, user.id);
    navigate("/problems");
  } catch (err) {
    console.error('Error creating problem:', err);
    setError(err?.data?.message || "Failed to create problem. Check console for details.");
  } finally {
    setLoading(false);
  }
};
  

  if (!isAdmin) {
    return null;
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ maxWidth: '800px', margin: '0 auto' }}>
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

        <form onSubmit={handleSubmit} style={{ background: 'white', padding: '32px', borderRadius: '8px', border: '1px solid #e8e8e8' }}>
          <div style={{ display: 'grid', gap: '24px' }}>
            {/* Problem Title */}
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                Problem Title *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
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

            {/* Problem Slug */}
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                Problem Slug *
              </label>
              <input
                type="text"
                value={slug}
                onChange={(e) => setSlug(e.target.value)}
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
                URL-friendly identifier (auto-generated from title)
              </small>
            </div>

            {/* Description */}
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                Problem Description *
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe the problem, input format, output format, and examples..."
                rows="8"
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

            {/* Difficulty and Category */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#333' }}>
                  Difficulty *
                </label>
                <select
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
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
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
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

            {/* Test Cases */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <label style={{ fontWeight: '600', color: '#333' }}>
                  Test Cases *
                </label>
                <button
                  type="button"
                  onClick={handleAddTestCase}
                  style={{
                    padding: '8px 16px',
                    background: '#28a745',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '14px'
                  }}
                >
                  + Add Test Case
                </button>
              </div>

              {testCases.map((testCase, index) => (
                <div key={index} style={{ 
                  border: '1px solid #e8e8e8', 
                  borderRadius: '6px', 
                  padding: '16px', 
                  marginBottom: '16px',
                  background: '#fafafa'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <h4 style={{ margin: 0, color: '#333' }}>Test Case {index + 1}</h4>
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

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '4px', fontSize: '14px', color: '#666' }}>
                        Input
                      </label>
                      <textarea
                        value={testCase.input}
                        onChange={(e) => handleTestCaseChange(index, 'input', e.target.value)}
                        placeholder="Test input"
                        rows="3"
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
                        Expected Output
                      </label>
                      <textarea
                        value={testCase.expectedOutput}
                        onChange={(e) => handleTestCaseChange(index, 'expectedOutput', e.target.value)}
                        placeholder="Expected output"
                        rows="3"
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

                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={testCase.isSample}
                      onChange={(e) => handleTestCaseChange(index, 'isSample', e.target.checked)}
                    />
                    <span style={{ fontSize: '14px', color: '#666' }}>Show as sample test case to users</span>
                  </label>
                </div>
              ))}
            </div>

            {/* Submit Button */}
            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
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
                  fontSize: '16px'
                }}
              >
                {loading ? 'Creating...' : 'Create Problem'}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}