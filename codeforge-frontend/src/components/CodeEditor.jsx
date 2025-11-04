import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Editor from '@monaco-editor/react';
import { executionAPI, userAPI } from '../services/api';
import ProblemDetailsView from './ProblemDetailsView';
import TestCasesSection from './TestCasesSection';
import Submissions from './Submissions';

const CodeEditor = ({ problem, onBack }) => {
  const [activeTab, setActiveTab] = useState('editor');
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('JAVA');
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [executionResult, setExecutionResult] = useState(null);
  const [submissionResult, setSubmissionResult] = useState(null);
  const [submissionsKey, setSubmissionsKey] = useState(0);

  const languages = [
    { value: 'JAVA', label: 'Java' },
    { value: 'PYTHON', label: 'Python' },
    { value: 'JAVASCRIPT', label: 'JavaScript' },
    { value: 'C', label: 'C' },
    { value: 'CPP', label: 'C++' }
  ];

  const tabs = [
    { id: 'editor', name: 'Code Editor', icon: '💻' },
    { id: 'submissions', name: 'Submissions', icon: '📋' }
  ];

  // Parse parameters safely (handles JSON string or array)
  const parseParameters = useMemo(() => {
    try {
      return typeof problem?.parameters === 'string' 
        ? JSON.parse(problem.parameters) 
        : (Array.isArray(problem.parameters) ? problem.parameters : []);
    } catch (error) {
      console.error('Failed to parse parameters:', error);
      return [];
    }
  }, [problem?.parameters]);

  // Map Java types to language-specific types
  const mapTypeToLanguage = (javaType, targetLanguage) => {
    const typeMap = {
      'JAVA': {
        'int[]': 'int[]',
        'String[]': 'String[]',
        'List<Integer>': 'List<Integer>',
        'List<String>': 'List<String>',
        'int': 'int',
        'String': 'String',
        'boolean': 'boolean',
        'double': 'double',
        'string': 'String',
        'string[]': 'String[]',
        'number': 'int',
        'number[]': 'int[]'
      },
      'PYTHON': {
        'int[]': 'List[int]',
        'String[]': 'List[str]',
        'List<Integer>': 'List[int]',
        'List<String>': 'List[str]',
        'int': 'int',
        'String': 'str',
        'boolean': 'bool',
        'double': 'float',
        'string': 'str',
        'string[]': 'List[str]',
        'number': 'int',
        'number[]': 'List[int]'
      },
      'JAVASCRIPT': {
        'int[]': 'number[]',
        'String[]': 'string[]',
        'List<Integer>': 'number[]',
        'List<String>': 'string[]',
        'int': 'number',
        'String': 'string',
        'boolean': 'boolean',
        'double': 'number',
        'string': 'string',
        'string[]': 'string[]',
        'number': 'number',
        'number[]': 'number[]'
      },
      'CPP': {
        'int[]': 'vector<int>',
        'String[]': 'vector<string>',
        'List<Integer>': 'vector<int>',
        'List<String>': 'vector<string>',
        'int': 'int',
        'String': 'string',
        'boolean': 'bool',
        'double': 'double'
      },
      'C': {
        'int[]': 'int*',
        'String[]': 'char**',
        'List<Integer>': 'int*',
        'List<String>': 'char**',
        'int': 'int',
        'String': 'char*',
        'boolean': 'bool',
        'double': 'double'
      }
    };

    return typeMap[targetLanguage]?.[javaType] || javaType;
  };

  // Map Java return types to language-specific return types
  const mapReturnTypeToLanguage = (javaReturnType, targetLanguage) => {
    const returnTypeMap = {
      'JAVA': {
        'int[]': 'int[]',
        'String[]': 'String[]',
        'List<Integer>': 'List<Integer>',
        'List<String>': 'List<String>',
        'int': 'int',
        'String': 'String',
        'boolean': 'boolean',
        'double': 'double',
        'string': 'String',
        'string[]': 'String[]',
        'number': 'int',
        'number[]': 'int[]',
        'void': 'void'
      },
      'PYTHON': {
        'int[]': 'List[int]',
        'String[]': 'List[str]',
        'List<Integer>': 'List[int]',
        'List<String>': 'List[str]',
        'int': 'int',
        'String': 'str',
        'boolean': 'bool',
        'double': 'float',
        'string': 'str',
        'string[]': 'List[str]',
        'number': 'int',
        'number[]': 'List[int]',
        'void': 'None'
      },
      'JAVASCRIPT': {
        'int[]': 'number[]',
        'String[]': 'string[]',
        'List<Integer>': 'number[]',
        'List<String>': 'string[]',
        'int': 'number',
        'String': 'string',
        'boolean': 'boolean',
        'double': 'number',
        'string': 'string',
        'string[]': 'string[]',
        'number': 'number',
        'number[]': 'number[]',
        'void': 'void'
      },
      'CPP': {
        'int[]': 'vector<int>',
        'String[]': 'vector<string>',
        'List<Integer>': 'vector<int>',
        'List<String>': 'vector<string>',
        'int': 'int',
        'String': 'string',
        'boolean': 'bool',
        'double': 'double',
        'void': 'void'
      },
      'C': {
        'int[]': 'int*',
        'String[]': 'char**',
        'List<Integer>': 'int*',
        'List<String>': 'char**',
        'int': 'int',
        'String': 'char*',
        'boolean': 'bool',
        'double': 'double',
        'void': 'void'
      }
    };

    return returnTypeMap[targetLanguage]?.[javaReturnType] || javaReturnType;
  };

  const setDefaultCode = useCallback(() => {
    if (problem?.codeTemplates?.[language]?.visibleCode) {
      setCode(problem.codeTemplates[language].visibleCode);
      return;
    }

    // Use actual problem values or sane fallbacks
    const actualReturnType = problem?.returnType || 'String';
    const actualFunctionName = problem?.functionName || 'solve';
    const actualParamsStr = getParametersString(language);

    const fallbackTemplates = {
      JAVA: `public class Solution {
    public ${mapReturnTypeToLanguage(actualReturnType, 'JAVA')} ${actualFunctionName}(${actualParamsStr}) {
        // Write your code here
        ${getDefaultReturnValue('JAVA', actualReturnType)}
    }
}`,
      PYTHON: `class Solution:
    def ${actualFunctionName}(self${actualParamsStr ? ', ' + actualParamsStr : ''}) -> ${mapReturnTypeToLanguage(actualReturnType, 'PYTHON')}:
        # Write your code here
        ${getDefaultReturnValue('PYTHON', actualReturnType)}`,
      JAVASCRIPT: `/**
 * @param {${getJSDocParameters()}}
 * @return {${mapReturnTypeToLanguage(actualReturnType, 'JAVASCRIPT')}}
 */
var ${actualFunctionName} = function(${actualParamsStr}) {
    // Write your code here
    ${getDefaultReturnValue('JAVASCRIPT', actualReturnType)}
};`,
      C: `#include <stdio.h>
#include <stdlib.h>

${mapReturnTypeToLanguage(actualReturnType, 'C')} ${actualFunctionName}(${actualParamsStr}) {
    // Write your code here
    ${getDefaultReturnValue('C', actualReturnType)}
}`,
      CPP: `class Solution {
public:
    ${mapReturnTypeToLanguage(actualReturnType, 'CPP')} ${actualFunctionName}(${actualParamsStr}) {
        // Write your code here
        ${getDefaultReturnValue('CPP', actualReturnType)}
    }
};`
    };
    setCode(fallbackTemplates[language] || fallbackTemplates.JAVA);
  }, [problem, language, parseParameters]);  // Depend on parsed params

  useEffect(() => {
    if (problem) {
      setDefaultCode();
    }
  }, [problem, language, setDefaultCode]);

  const getDefaultReturnValue = (lang, returnType) => {
    const returnValues = {
      'JAVA': {
        'int[]': 'return new int[0];',
        'String[]': 'return new String[0];',
        'List<Integer>': 'return new ArrayList<>();',
        'List<String>': 'return new ArrayList<>();',
        'int': 'return 0;',
        'String': 'return "";',
        'boolean': 'return false;',
        'double': 'return 0.0;',
        'void': ''
      },
      'PYTHON': {
        'int[]': 'return []',
        'String[]': 'return []',
        'List<Integer>': 'return []',
        'List<String>': 'return []',
        'int': 'return 0',
        'String': 'return ""',
        'boolean': 'return False',
        'double': 'return 0.0',
        'string': 'return ""',
        'string[]': 'return []',
        'number': 'return 0',
        'number[]': 'return []',
        'void': 'pass'  // Fixed: Use pass for void
      },
      'JAVASCRIPT': {
        'int[]': 'return [];',
        'String[]': 'return [];',
        'List<Integer>': 'return [];',
        'List<String>': 'return [];',
        'int': 'return 0;',
        'String': 'return "";',
        'boolean': 'return false;',
        'double': 'return 0.0;',
        'void': 'return;'
      },
      'CPP': {
        'int[]': 'return {};',
        'String[]': 'return {};',
        'List<Integer>': 'return {};',
        'List<String>': 'return {};',
        'int': 'return 0;',
        'String': 'return "";',
        'boolean': 'return false;',
        'double': 'return 0.0;',
        'void': 'return;'
      },
      'C': {
        'int[]': 'return NULL;',  // Fixed: Added semicolon
        'String[]': 'return NULL;',
        'List<Integer>': 'return NULL;',
        'List<String>': 'return NULL;',
        'int': 'return 0;',
        'String': 'return "";',
        'boolean': 'return false;',
        'double': 'return 0.0;',
        'void': 'return;'  // Fixed: Added semicolon
      }
    };

    return returnValues[lang]?.[returnType] || (lang === 'PYTHON' ? 'pass' : 'return null;');
  };

  const getParametersString = (targetLanguage) => {
    if (parseParameters.length === 0) return '';

    if (targetLanguage === 'PYTHON') {
      return parseParameters.map(p => p.name).join(', ');  // self is prepended in template
    } else if (targetLanguage === 'JAVASCRIPT') {
      return parseParameters.map(p => p.name).join(', ');
    } else {
      return parseParameters.map(p => {
        const mappedType = mapTypeToLanguage(p.type, targetLanguage);
        return `${mappedType} ${p.name}`;
      }).join(', ');
    }
  };

  const getJSDocParameters = () => {
    if (parseParameters.length === 0) return '';
    return parseParameters.map(p => {
      const jsType = mapTypeToLanguage(p.type, 'JAVASCRIPT');
      return `${jsType} ${p.name}`;
    }).join(', ');
  };

  const hasCode = code.trim().length > 0;

  const handleRunCode = async () => {
    if (!hasCode) return;  // Validation
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
      console.error('Execution error:', error);
      setExecutionResult({
        status: 'ERROR',
        message: error.response?.data?.message || error.message || 'Execution failed'
      });
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmitSolution = async () => {
    if (!hasCode) return;  // Validation
    setIsSubmitting(true);
    setSubmissionResult(null);
    try {
      const request = {
        code,
        language
      };
      
      // Use the correct endpoint with problemId in the URL
      const response = await userAPI.submitSolution(problem.id, request);
      setSubmissionResult(response.data);
      
      // Refresh submissions
      setSubmissionsKey(prev => prev + 1);
      
    } catch (error) {
      console.error('Submission error:', error);
      setSubmissionResult({
        status: 'ERROR',
        message: error.response?.data?.message || error.message || 'Submission failed'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderExecutionResult = () => {
    if (!executionResult) return null;

    return (
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
        <h3 className="font-medium text-white mb-2">Execution Result</h3>
        <div className={`p-3 rounded ${
          executionResult.status === 'SUCCESS' 
            ? 'bg-green-900/30 text-green-300 border border-green-600' 
            : 'bg-red-900/30 text-red-300 border border-red-600'
        }`}>
          <div className="flex items-center justify-between mb-2">
            <div>
              <strong>Status:</strong> {executionResult.status || 'Unknown'}
            </div>
            {executionResult.totalExecutionTime && (
              <div className="text-sm">
                Time: {executionResult.totalExecutionTime}ms
              </div>
            )}
          </div>

          {executionResult.testCaseResults && (
            <div className="mt-3">
              <strong>Test Case Results:</strong>
              <div className="mt-2 space-y-2">
                {executionResult.testCaseResults.map((result, index) => (
                  <div key={index} className={`p-2 rounded text-sm ${
                    result.status === 'PASSED' 
                      ? 'bg-green-800/30 text-green-300' 
                      : 'bg-red-800/30 text-red-300'
                  }`}>
                    <div className="flex justify-between">
                      <span>Test Case {index + 1}: {result.status}</span>
                      {result.executionTime && (
                        <span>{result.executionTime}ms</span>
                      )}
                    </div>
                    {result.actualOutput && (
                      <div className="mt-1">
                        <strong>Output:</strong> {result.actualOutput}
                      </div>
                    )}
                    {result.errorMessage && (
                      <div className="mt-1">
                        <strong>Error:</strong> {result.errorMessage}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {executionResult.compilationError && (
            <div className="mt-2">
              <strong>Compilation Error:</strong>
              <pre className="mt-1 whitespace-pre-wrap text-sm bg-black/30 p-2 rounded">
                {executionResult.compilationError}
              </pre>
            </div>
          )}

          {executionResult.message && (
            <div className="mt-2">
              <strong>Message:</strong> {executionResult.message}
            </div>
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
          submissionResult.status === 'ACCEPTED' 
            ? 'bg-green-900/30 text-green-300 border border-green-600' 
            : submissionResult.status === 'ERROR'
            ? 'bg-red-900/30 text-red-300 border border-red-600'
            : 'bg-yellow-900/30 text-yellow-300 border border-yellow-600'
        }`}>
          <div className="flex items-center justify-between">
            <div>
              <strong>Status:</strong> {submissionResult.status || 'Unknown'}
            </div>
            {submissionResult.executionTime && (
              <div className="text-sm">
                Time: {submissionResult.executionTime}ms
              </div>
            )}
            {submissionResult.memoryUsed && (
              <div className="text-sm">
                Memory: {submissionResult.memoryUsed}MB
              </div>
            )}
          </div>
          
          {submissionResult.passedTestCases !== undefined && submissionResult.totalTestCases !== undefined && (
            <div className="mt-2">
              <strong>Test Cases:</strong> {submissionResult.passedTestCases}/{submissionResult.totalTestCases} passed
            </div>
          )}
          
          {submissionResult.errorMessage && (
            <div className="mt-2">
              <strong>Error:</strong> {submissionResult.errorMessage}
            </div>
          )}
          
          {submissionResult.actualOutput && submissionResult.expectedOutput && (
            <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <strong className="text-red-400">Your Output:</strong>
                <pre className="mt-1 whitespace-pre-wrap bg-black/30 p-2 rounded text-sm">
                  {submissionResult.actualOutput}
                </pre>
              </div>
              <div>
                <strong className="text-green-400">Expected Output:</strong>
                <pre className="mt-1 whitespace-pre-wrap bg-black/30 p-2 rounded text-sm">
                  {submissionResult.expectedOutput}
                </pre>
              </div>
            </div>
          )}

          {submissionResult.id && (
            <div className="mt-3 pt-3 border-t border-gray-600 text-sm text-gray-400">
              Submission ID: {submissionResult.id}
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderEditorTab = () => {
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
            disabled={!hasCode || isRunning}
            className="px-6 py-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white rounded-md font-medium transition-colors"
          >
            {isRunning ? 'Running...' : 'Run Code'}
          </button>
          <button
            onClick={handleSubmitSolution}
            disabled={!hasCode || isSubmitting}
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
  };

  const renderActiveTab = () => {
    switch (activeTab) {
      case 'editor':
        return renderEditorTab();
      case 'submissions':
        return <Submissions problemId={problem.id} key={submissionsKey} />;
      default:
        return null;
    }
  };

  if (!problem) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-900">
        <div className="text-center">
          <div className="text-2xl text-gray-400 mb-4">No problem selected</div>
          <button
            onClick={onBack}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-medium transition-colors"
          >
            Back to Problems
          </button>
        </div>
      </div>
    );
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
                <div className="flex items-center space-x-2 mt-1">
                  <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
                    problem.difficulty === 'EASY' 
                      ? 'bg-green-900 text-green-300'
                      : problem.difficulty === 'MEDIUM'
                      ? 'bg-yellow-900 text-yellow-300'
                      : 'bg-red-900 text-red-300'
                  }`}>
                    {problem.difficulty}
                  </span>
                  <span className="text-sm text-gray-400">
                    {problem.category?.name || 'Uncategorized'}
                  </span>
                </div>
              </div>
            </div>
            <div className="text-sm text-gray-400">
              Problem ID: {problem.id}
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