import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { executionAPI, userAPI } from '../services/api'; // Import both APIs
import ProblemDetailsView from './ProblemDetailsView';
import TestCasesSection from './TestCasesSection';

const CodeEditor = ({ problem, onBack }) => {
  const [activeTab, setActiveTab] = useState('editor');
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('JAVA');
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [executionResult, setExecutionResult] = useState(null);
  const [submissionResult, setSubmissionResult] = useState(null);

  const languages = [
    { value: 'JAVA', label: 'Java' },
    { value: 'PYTHON', label: 'Python' },
    { value: 'JAVASCRIPT', label: 'JavaScript' },
    { value: 'C', label: 'C' },
    { value: 'CPP', label: 'C++' }
  ];

  const tabs = [
    { id: 'editor', name: 'Code Editor', icon: '💻' }
  ];

  useEffect(() => {
  if (problem) {
    setDefaultCode();
    console.log("CodeEditor - Problem received:", problem);
    console.log("CodeEditor - Test cases:", problem.testCases);
    console.log("CodeEditor - Sample test cases:", problem.testCases?.filter(tc => tc.isSample));
  }
}, [problem, language]);


  const setDefaultCode = () => {
    if (!problem?.codeTemplates?.[language]?.visibleCode) {
      // Fallback to basic template if no stored template exists
      const fallbackTemplates = {
        JAVA: `public class Solution {
    public ${problem?.returnType || 'int[][]'} ${problem?.functionName || 'solve'}(${getParametersString()}) {
        // Write your code here
        
    }
}`,
        PYTHON: `class Solution:
    def ${problem?.functionName || 'solve'}(self${getParametersString(true)}):
        # Write your code here
        pass`,
        JAVASCRIPT: `/**
 * @param {${getParametersString(true)}}
 * @return {${problem?.returnType || 'number[][]'}}
 */
var ${problem?.functionName || 'solve'} = function(${getParametersString(true)}) {
    // Write your code here
    
};`,
        C: `#include <stdio.h>
#include <stdlib.h>

${problem?.returnType || 'int**'} ${problem?.functionName || 'solve'}(${getParametersString()}) {
    // Write your code here

}`,
        CPP: `class Solution {
public:
    ${problem?.returnType || 'vector<vector<int>>'} ${problem?.functionName || 'solve'}(${getParametersString()}) {
        // Write your code here
        
    }
};`
      };
      setCode(fallbackTemplates[language] || '');
      return;
    }

    // Use the stored template from the problem
    setCode(problem.codeTemplates[language].visibleCode);
  };

  const getParametersString = (isPython = false) => {
    if (!problem?.parameters) return '';
    try {
      const params = JSON.parse(problem.parameters);
      if (isPython) {
        return params.length > 0 ? ', ' + params.map(p => p.name).join(', ') : '';
      }
      return params.map(p => `${p.type} ${p.name}`).join(', ');
    } catch {
      return '';
    }
  };

  const handleRunCode = async () => {
    setIsRunning(true);
    setExecutionResult(null);
    try {
      // Get sample test cases from the problem
      const sampleTestCases = problem?.testCases?.filter(tc => tc.isSample) || [];

      if (sampleTestCases.length === 0) {
        setExecutionResult({
          status: 'ERROR',
          message: 'No sample test cases available for this problem'
        });
        return;
      }

      // Transform test cases to match backend format
      const testCases = sampleTestCases.map((tc, index) => ({
        testCaseId: tc.id || index + 1,
        inputData: tc.inputData,
        expectedOutput: tc.expectedOutput,
        weight: tc.weight || 1
      }));

      const request = {
        combinedCode: code,
        language: language,
        testCases: testCases,
        timeLimitMs: problem?.timeLimitMs || 5000,
        memoryLimitMb: problem?.memoryLimitMb || 256
      };

      console.log('Sending execution request:', request);
      const response = await executionAPI.runCode(request);
      console.log('Execution response:', response.data);
      setExecutionResult(response.data);
    } catch (error) {
      console.error('Execution error:', error);
      setExecutionResult({
        status: 'ERROR',
        message: error.response?.data?.compilationError || 
                error.response?.data?.message || 
                error.message || 
                'Execution failed'
      });
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmitSolution = async () => {
    setIsSubmitting(true);
    setSubmissionResult(null);
    try {
      const request = {
        code: code,
        language: language
      };
      
      console.log('Submitting solution for problem:', problem.id);
      const response = await userAPI.submitSolution(problem.id, request);
      console.log('Submission response:', response.data);
      
      setSubmissionResult({
        status: response.data.status,
        message: getSubmissionMessage(response.data.status),
        data: response.data
      });
    } catch (error) {
      console.error('Submission error:', error);
      setSubmissionResult({
        status: 'ERROR',
        message: error.response?.data?.message || 
                error.message || 
                'Submission failed'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const getSubmissionMessage = (status) => {
    switch (status) {
      case 'ACCEPTED': return '✅ Solution Accepted! All test cases passed.';
      case 'WRONG_ANSWER': return '❌ Wrong Answer. Check your logic.';
      case 'TIME_LIMIT_EXCEEDED': return '⏰ Time Limit Exceeded. Optimize your solution.';
      case 'RUNTIME_ERROR': return '💥 Runtime Error. Check for exceptions.';
      case 'COMPILATION_ERROR': return '🔧 Compilation Error. Check your syntax.';
      default: return `Status: ${status}`;
    }
  };

  const renderExecutionResult = () => {
    if (!executionResult) return null;

    return (
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
        <h3 className="font-medium text-white mb-2">Execution Result</h3>
        <div className={`p-3 rounded ${
          executionResult.status === 'SUCCESS' ? 'bg-green-900/30 text-green-300' : 
          executionResult.status === 'COMPILATION_ERROR' ? 'bg-red-900/30 text-red-300' :
          'bg-yellow-900/30 text-yellow-300'
        }`}>
          {executionResult.status === 'SUCCESS' && executionResult.testCaseResults && (
            <div>
              <div className="mb-2">
                <strong>Test Cases:</strong> {executionResult.testCaseResults.filter(tc => tc.status === 'PASSED').length} / {executionResult.testCaseResults.length} passed
              </div>
              {executionResult.testCaseResults.map((tc, index) => (
                <div key={index} className="text-sm mb-1">
                  <span className={tc.status === 'PASSED' ? 'text-green-400' : 'text-red-400'}>
                    • Test {index + 1}: {tc.status}
                  </span>
                </div>
              ))}
            </div>
          )}
          {executionResult.compilationError && (
            <div>
              <strong>Compilation Error:</strong>
              <pre className="mt-1 whitespace-pre-wrap">{executionResult.compilationError}</pre>
            </div>
          )}
          {executionResult.message && !executionResult.compilationError && (
            <pre className="whitespace-pre-wrap">{executionResult.message}</pre>
          )}
        </div>
      </div>
    );
  };

  const renderSubmissionResult = () => {
    if (!submissionResult) return null;

    return (
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
        <h3 className="font-medium text-white mb-2">Submission Result</h3>
        <div className={`p-3 rounded ${
          submissionResult.status === 'ACCEPTED' ? 'bg-green-900/30 text-green-300' : 
          'bg-red-900/30 text-red-300'
        }`}>
          <div className="font-semibold">{submissionResult.message}</div>
          {submissionResult.data && (
            <div className="mt-2 text-sm">
              <div>Execution Time: {submissionResult.data.executionTime || 0}ms</div>
              <div>Memory Used: {submissionResult.data.memoryUsed || 0}MB</div>
              <div>Test Cases: {submissionResult.data.passedTestCases || 0}/{submissionResult.data.totalTestCases || 0}</div>
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderActiveTab = () => {
    switch (activeTab) {
      case 'editor':
        return (
          <div className="space-y-4">
            {/* Language Selector */}
            <div className="flex items-center space-x-4">
              <label className="text-sm font-medium text-gray-200">Language:</label>
              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                className="px-3 py-1 rounded-md bg-gray-800 border border-gray-600 text-white focus:border-blue-500 focus:outline-none"
              >
                {languages.map(lang => (
                  <option key={lang.value} value={lang.value}>{lang.label}</option>
                ))}
              </select>
            </div>

            {/* Code Editor */}
            <div className="border border-gray-600 rounded-lg overflow-hidden">
              <Editor
                height="400px"
                language={language.toLowerCase()}
                value={code}
                onChange={setCode}
                theme="vs-dark"
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  lineNumbers: 'on',
                  roundedSelection: false,
                  scrollBeyondLastLine: false,
                  automaticLayout: true,
                }}
              />
            </div>

            {/* Test Cases Section */}
            <TestCasesSection problem={problem} />

            {/* Action Buttons */}
            <div className="flex space-x-4">
              <button
                onClick={handleRunCode}
                disabled={isRunning}
                className="px-6 py-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white rounded-md font-medium transition-colors"
              >
                {isRunning ? 'Running...' : 'Run Code'}
              </button>
              <button
                onClick={handleSubmitSolution}
                disabled={isSubmitting}
                className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white rounded-md font-medium transition-colors"
              >
                {isSubmitting ? 'Submitting...' : 'Submit Solution'}
              </button>
            </div>

            {/* Execution Result */}
            {renderExecutionResult()}

            {/* Submission Result */}
            {renderSubmissionResult()}
          </div>
        );
      default:
        return null;
    }
  };

  if (!problem) {
    return <div className="p-6 text-center text-gray-400">No problem selected</div>;
  }

  return (
    <div className="h-screen flex bg-gray-900">
      {/* Problem Details Sidebar */}
      <div className="w-1/3 bg-gray-800 border-r border-gray-600 overflow-auto">
        <div className="p-6">
          <ProblemDetailsView problem={problem} />
        </div>
      </div>

      {/* Code Editor Section */}
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="bg-gray-800 border-b border-gray-600 px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                onClick={onBack}
                className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors"
              >
                <span>←</span>
                <span>Back to Problems</span>
              </button>
              <div className="h-6 w-px bg-gray-600"></div>
              <div>
                <h1 className="text-xl font-bold text-white">{problem.title}</h1>
                <div className="text-sm text-gray-400">ID: {problem.id}</div>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-gray-800 border-b border-gray-600">
          <div className="px-6">
            <nav className="flex space-x-8">
              {tabs.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center space-x-2 py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-400'
                      : 'border-transparent text-gray-400 hover:text-white'
                  }`}
                >
                  <span>{tab.icon}</span>
                  <span>{tab.name}</span>
                </button>
              ))}
            </nav>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-auto p-6 bg-gray-900">
          {renderActiveTab()}
        </div>
      </div>
    </div>
  );
};

export default CodeEditor;