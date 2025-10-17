import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getProblem, submitSolution } from "../api";
import CodeEditor from "./CodeEditor";

export default function ProblemDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [problem, setProblem] = useState(null);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('description');
  const [submissionResult, setSubmissionResult] = useState(null);
  const [loading, setLoading] = useState(true);

  // Enhanced fetchProblem function
  const fetchProblem = async (problemId) => {
    try {
      console.log("🔄 Fetching problem with ID:", problemId);
      const response = await fetch(`http://localhost:8080/api/problems/${problemId}`);
      
      console.log("📡 Response status:", response.status);
      console.log("📡 Response URL:", response.url);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const problemData = await response.json();
      console.log("✅ Problem data received:", problemData);
      return problemData;
      
    } catch (error) {
      console.error("❌ Error fetching problem:", error);
      throw error;
    }
  };

  useEffect(() => {
    const loadProblem = async () => {
      try {
        setLoading(true);
        const problemData = await fetchProblem(id);
        
        // EXTENSIVE DEBUGGING
        console.log("=== PROBLEM DATA DEBUG ===");
        console.log("Problem ID:", problemData.id);
        console.log("Problem Title:", problemData.title);
        console.log("Has codeTemplates:", !!problemData.codeTemplates);
        console.log("codeTemplates array:", problemData.codeTemplates);
        console.log("codeTemplates length:", problemData.codeTemplates?.length);
        
        if (problemData.codeTemplates && problemData.codeTemplates.length > 0) {
          problemData.codeTemplates.forEach((template, index) => {
            console.log(`--- Template ${index} ---`);
            console.log("Language:", template.language);
            console.log("TemplateCode field exists:", 'templateCode' in template);
            console.log("TemplateCode value:", template.templateCode);
            console.log("TemplateCode length:", template.templateCode?.length);
            console.log("All template keys:", Object.keys(template));
          });
        } else {
          console.log("❌ NO CODE TEMPLATES FOUND IN PROBLEM DATA");
        }
        
        setProblem(problemData);
      } catch (err) {
        setError(err?.message || 'Failed to fetch problem');
      } finally {
        setLoading(false);
      }
    };

    loadProblem();
  }, [id]);

  const handleCodeSubmit = async (submission) => {
    try {
      setSubmissionResult({ status: 'testing', message: 'Running test cases...' });
      const result = await submitSolution(
        submission.problemId,
        submission.userId,
        submission.code,
        submission.language
      );
      setSubmissionResult({ 
        status: result.passed ? 'success' : 'error',
        message: result.passed 
          ? 'All test cases passed! 🎉' 
          : `Test cases failed. ${result.message || 'Try again!'}`,
        details: result
      });
    } catch (err) {
      setSubmissionResult({ 
        status: 'error', 
        message: err?.data?.message || 'Submission failed' 
      });
    }
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center',
        height: 'calc(100vh - 60px)'
      }}>
        <div className="loading">
          <div className="loading-spinner"></div>
          Loading problem...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="error">
          {error}
        </div>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="error">
          Problem not found
        </div>
      </div>
    );
  }

  return (
    <div style={{ 
      display: 'grid', 
      gridTemplateColumns: '1fr 1fr', 
      height: 'calc(100vh - 60px)',
      background: '#fff'
    }}>
      {/* Problem Description Panel */}
      <div style={{ 
        borderRight: '1px solid #e8e8e8',
        overflow: 'auto',
        background: 'white'
      }}>
        <div style={{ padding: '32px' }}>
          <div style={{ marginBottom: '24px' }}>
            <h1 style={{ 
              fontSize: '28px', 
              fontWeight: '700', 
              marginBottom: '8px',
              color: '#333'
            }}>
              {problem.title}
            </h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <span className={`difficulty-badge difficulty-${problem.difficulty?.toLowerCase()}`}>
                {problem.difficulty}
              </span>
              <div style={{ display: 'flex', gap: '8px' }}>
                {problem.categories?.map(cat => (
                  <span 
                    key={cat.id}
                    style={{
                      padding: '4px 8px',
                      background: '#e9ecef',
                      borderRadius: '4px',
                      fontSize: '12px',
                      color: '#666'
                    }}
                  >
                    {cat.name}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Tabs */}
          <div className="tabs">
            <button
              className={`tab ${activeTab === 'description' ? 'active' : ''}`}
              onClick={() => setActiveTab('description')}
            >
              Description
            </button>
            <button
              className={`tab ${activeTab === 'solutions' ? 'active' : ''}`}
              onClick={() => setActiveTab('solutions')}
            >
              Solutions
            </button>
            <button
              className={`tab ${activeTab === 'submissions' ? 'active' : ''}`}
              onClick={() => setActiveTab('submissions')}
            >
              Submissions
            </button>
          </div>

          {/* Tab Content */}
          <div style={{ minHeight: '400px' }}>
            {activeTab === 'description' && (
              <div>
                <div 
                  style={{ 
                    fontSize: '16px', 
                    lineHeight: '1.6',
                    color: '#333'
                  }}
                  dangerouslySetInnerHTML={{ 
                    __html: problem.description?.replace(/\n/g, '<br>') || problem.description 
                  }}
                />
                
                {/* Test Cases */}
                {problem.testCases && problem.testCases.length > 0 && (
                  <div style={{ marginTop: '32px' }}>
                    <h3 style={{ marginBottom: '16px', fontSize: '20px', fontWeight: '600' }}>
                      Test Cases
                    </h3>
                    <div>
                      {problem.testCases.map((testCase, index) => (
                        <div 
                          key={testCase.id}
                          className={`test-case ${testCase.isSample ? 'test-case-sample' : ''}`}
                        >
                          <div className="test-case-header">
                            <span className="test-case-title">
                              Test Case {index + 1}
                              {testCase.isSample && (
                                <span style={{ 
                                  marginLeft: '8px',
                                  color: '#007bff',
                                  fontSize: '12px'
                                }}>
                                  (Sample)
                                </span>
                              )}
                            </span>
                          </div>
                          <div style={{ fontFamily: 'monospace', fontSize: '14px' }}>
                            <div><strong>Input:</strong> {testCase.inputData}</div>
                            <div><strong>Expected Output:</strong> {testCase.expectedOutput}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'solutions' && (
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <h3 style={{ marginBottom: '12px', color: '#666' }}>Solutions</h3>
                <p style={{ color: '#999' }}>
                  Solutions will be available after you solve the problem.
                </p>
              </div>
            )}

            {activeTab === 'submissions' && (
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <h3 style={{ marginBottom: '12px', color: '#666' }}>Submissions</h3>
                <p style={{ color: '#999' }}>
                  Your submissions will appear here after you submit solutions.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Code Editor Panel */}
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        background: '#f8f9fa'
      }}>
        <div style={{ 
          padding: '24px 32px',
          borderBottom: '1px solid #e8e8e8',
          background: 'white'
        }}>
          <h2 style={{ 
            fontSize: '20px', 
            fontWeight: '600',
            marginBottom: '8px'
          }}>
            Code Editor
          </h2>
          <p style={{ color: '#666', fontSize: '14px' }}>
            Write your solution in the editor below
          </p>
        </div>

        <div style={{ flex: 1, padding: '24px' }}>
          {user ? (
            <>
              {/* FIX: Pass problem data to CodeEditor */}
              <CodeEditor 
                onCodeSubmit={handleCodeSubmit}
                problemId={problem.id}
                userId={user.id}
                problemData={problem} // ← THIS IS THE FIX!
              />
              
              {/* Submission Result */}
              {submissionResult && (
                <div 
                  className={submissionResult.status === 'success' ? 'success' : 'error'}
                  style={{ marginTop: '16px' }}
                >
                  <div style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'space-between' 
                  }}>
                    <span>{submissionResult.message}</span>
                    {submissionResult.status === 'testing' && (
                      <div className="loading-spinner" style={{ margin: 0 }}></div>
                    )}
                  </div>
                  {submissionResult.details && (
                    <div style={{ marginTop: '8px', fontSize: '14px' }}>
                      <div>Time: {submissionResult.details.executionTime}ms</div>
                      <div>Memory: {submissionResult.details.memoryUsed}MB</div>
                    </div>
                  )}
                </div>
              )}
            </>
          ) : (
            <div className="card" style={{ 
              padding: '40px', 
              textAlign: 'center',
              background: 'white'
            }}>
              <h3 style={{ marginBottom: '12px', color: '#666' }}>
                Sign In Required
              </h3>
              <p style={{ color: '#999', marginBottom: '20px' }}>
                Please sign in to submit solutions
              </p>
              <a href="/login" className="btn btn-primary">
                Sign In
              </a>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}