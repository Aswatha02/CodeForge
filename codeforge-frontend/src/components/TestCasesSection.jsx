import React, { useState } from 'react';

const TestCasesSection = ({ problem }) => {
  const [activeTestCase, setActiveTestCase] = useState(0);

  // Debug logging to see what we're receiving
  console.log("TestCasesSection - Problem:", problem);
  console.log("TestCasesSection - Test cases:", problem?.testCases);
  console.log("TestCasesSection - Sample test cases:", problem?.testCases?.filter(tc => tc.isSample));

  if (!problem?.testCases || problem.testCases.length === 0) {
    return (
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
        <h3 className="font-medium text-white mb-2">Test Cases</h3>
        <div className="text-gray-400 text-sm">
          No test cases available for this problem.
        </div>
      </div>
    );
  }

  const sampleTestCases = problem.testCases.filter(tc => tc.isSample);

  if (sampleTestCases.length === 0) {
    return (
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-600">
        <h3 className="font-medium text-white mb-2">Test Cases</h3>
        <div className="text-gray-400 text-sm">
          No sample test cases available. All {problem.testCases.length} test cases are hidden.
        </div>
      </div>
    );
  }

  const currentTestCase = sampleTestCases[activeTestCase];

  const formatTestCaseData = (data) => {
    try {
      const parsed = JSON.parse(data);
      return JSON.stringify(parsed, null, 2);
    } catch (error) {
      console.log("Error parsing test case data:", error);
      return data;
    }
  };

  return (
    <div className="bg-gray-800 rounded-lg border border-gray-600">
      <div className="p-4 border-b border-gray-600">
        <h3 className="text-lg font-semibold text-white">Test Cases</h3>
        <p className="text-sm text-gray-400 mt-1">
          {sampleTestCases.length} sample test case(s) available
          {problem.testCases.length > sampleTestCases.length && 
            ` (${problem.testCases.length - sampleTestCases.length} hidden)`
          }
        </p>
      </div>

      {/* Test Case Navigation */}
      <div className="px-4 py-2 border-b border-gray-600">
        <div className="flex space-x-2 overflow-x-auto">
          {sampleTestCases.map((testCase, index) => (
            <button
              key={testCase.id || index}
              onClick={() => setActiveTestCase(index)}
              className={`px-3 py-1 rounded text-sm font-medium transition-colors whitespace-nowrap ${
                activeTestCase === index
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
            >
              Case {index + 1}
            </button>
          ))}
        </div>
      </div>

      {/* Test Case Content */}
      <div className="p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-400 mb-2">Input:</label>
            <pre className="bg-gray-900 p-3 rounded border border-gray-600 text-sm text-gray-200 overflow-x-auto font-mono whitespace-pre-wrap">
              {formatTestCaseData(currentTestCase.inputData)}
            </pre>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-400 mb-2">Expected Output:</label>
            <pre className="bg-gray-900 p-3 rounded border border-gray-600 text-sm text-gray-200 overflow-x-auto font-mono whitespace-pre-wrap">
              {currentTestCase.expectedOutput}
            </pre>
          </div>
        </div>

        {currentTestCase.explanation && currentTestCase.explanation.trim() !== '' && (
          <div className="mt-4">
            <label className="block text-sm font-medium text-gray-400 mb-2">Explanation:</label>
            <div className="bg-gray-900 p-3 rounded border border-gray-600 text-sm text-gray-300">
              {currentTestCase.explanation}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TestCasesSection;