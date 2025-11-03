import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { executionAPI } from '../services/api';
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
    }
  }, [problem, language]);



  const setDefaultCode = () => {
    if (!problem?.codeTemplates?.[language]?.visibleCode) {
      // Fallback to basic template if no stored template exists
      const fallbackTemplates = {
        JAVA: `public class Solution {
    public ${problem?.returnType || 'void'} ${problem?.functionName || 'solution'}(${getParametersString()}) {
        // Write your code here

    }
}`,
        PYTHON: `class Solution:
    def ${problem?.functionName || 'solution'}(self${getParametersString(true)}) -> ${problem?.returnType || 'None'}:
        # Write your code here
        pass`,
        JAVASCRIPT: `/**
 * @param {${getParametersString(true)}}
 * @return {${problem?.returnType || '*'}}
 */
var ${problem?.functionName || 'solution'} = function(${getParametersString(true)}) {
    // Write your code here

};`,
        C: `#include <stdio.h>
#include <stdlib.h>

${problem?.returnType || 'void'} ${problem?.functionName || 'solution'}(${getParametersString()}) {
    // Write your code here

}

int main() {
    // Test your function here
    return 0;
}`,
        CPP: `class Solution {
public:
    ${problem?.returnType || 'void'} ${problem?.functionName || 'solution'}(${getParametersString()}) {
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

      // Transform test cases to match backend format
      const testCases = sampleTestCases.map((tc, index) => ({
        testCaseId: tc.id || index + 1,
        inputData: tc.inputData,
        expectedOutput: tc.expectedOutput,
        weight: tc.weight || 1
      }));

      const request = {
        combinedCode: code,
        language,
        testCases,
        timeLimitMs: 5000, // 5 seconds default
        memoryLimitMb: 256 // 256MB default
      };

      const response = await executionAPI.runCode(request);
      setExecutionResult(response.data);
    } catch (error) {
      setExecutionResult({
        status: 'ERROR',
        message: error.response?.data?.message || 'Execution failed'
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
        code,
        language,
        problemId: problem.id
      };
      const response = await executionAPI.submitSolution(request);
      setSubmissionResult(response.data);
    } catch (error) {
      setSubmissionResult({
        status: 'ERROR',
        message: error.response?.data?.message || 'Submission failed'
      });
    } finally {
      setIsSubmitting(false);
    }
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
            {executionResult && (
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
                <h3 className="font-medium text-white mb-2">Execution Result</h3>
                <div className={`p-3 rounded ${executionResult.status === 'SUCCESS' ? 'bg-green-900/30 text-green-300' : 'bg-red-900/30 text-red-300'}`}>
                  <pre className="whitespace-pre-wrap text-sm text-gray-200">{executionResult.message || JSON.stringify(executionResult, null, 2)}</pre>
                </div>
              </div>
            )}

            {/* Submission Result */}
            {submissionResult && (
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
                <h3 className="font-medium text-white mb-2">Submission Result</h3>
                <div className={`p-3 rounded ${submissionResult.status === 'ACCEPTED' ? 'bg-green-900/30 text-green-300' : 'bg-red-900/30 text-red-300'}`}>
                  <pre className="whitespace-pre-wrap text-sm text-gray-200">{submissionResult.message || JSON.stringify(submissionResult, null, 2)}</pre>
                </div>
              </div>
            )}
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
