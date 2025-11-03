import React from 'react';

const ProblemDetailsView = ({ problem }) => {
  if (!problem) {
    return <div className="p-6 text-center text-gray-400">No problem selected</div>;
  }

  return (
    <div className="space-y-6 p-6 bg-gray-900 rounded-lg shadow-lg border border-gray-700">
      {/* Problem Header */}
      <div className="border-b border-gray-700 pb-4">
        <div className="flex items-center gap-3 mb-2">
          <span className="text-lg font-mono text-gray-400">#{problem.id}</span>
          <h1 className="text-2xl font-bold text-white">{problem.title}</h1>
        </div>
        <div className="flex items-center gap-4 text-sm">
          <span className={`font-semibold px-3 py-1 rounded text-xs ${
            problem.difficulty === 'EASY' ? 'bg-green-900 text-green-300' :
            problem.difficulty === 'MEDIUM' ? 'bg-yellow-900 text-yellow-300' :
            'bg-red-900 text-red-300'
          }`}>
            {problem.difficulty}
          </span>
          <span className="text-gray-300">
            Acceptance Rate: {problem.acceptanceRate || '0.0%'}
          </span>
        </div>
      </div>

      {/* Problem Description */}
      <div className="bg-gray-800 p-4 rounded-lg border border-gray-600">
        <h2 className="text-lg font-semibold text-white mb-3">Problem Description</h2>
        <div className="text-gray-200 leading-relaxed whitespace-pre-wrap">{problem.description}</div>
      </div>

      {/* Examples Section */}
      {problem.testCases && problem.testCases.filter(tc => tc.isSample).length > 0 && (
        <div className="bg-gray-800 p-4 rounded-lg border border-gray-600">
          <h3 className="text-lg font-semibold text-white mb-3">Examples</h3>
          <div className="space-y-6">
            {problem.testCases
              .filter(tc => tc.isSample)
              .map((testCase, index) => (
                <div key={testCase.id || index} className="border-b border-gray-700 pb-4 last:border-b-0">
                  <div className="text-sm font-medium text-gray-300 mb-2">Example {index + 1}:</div>
                  <div className="space-y-3">
                    <div>
                      <div className="text-xs font-medium text-gray-400 mb-1">Input:</div>
                      <pre className="bg-gray-900 p-3 rounded border border-gray-600 text-sm text-gray-200 overflow-x-auto font-mono whitespace-pre-wrap">{testCase.inputData}</pre>
                    </div>
                    <div>
                      <div className="text-xs font-medium text-gray-400 mb-1">Output:</div>
                      <pre className="bg-gray-900 p-3 rounded border border-gray-600 text-sm text-gray-200 overflow-x-auto font-mono whitespace-pre-wrap">{testCase.expectedOutput}</pre>
                    </div>
                    {testCase.explanation && testCase.explanation.trim() !== '' && (
                      <div>
                        <div className="text-xs font-medium text-gray-400 mb-1">Explanation:</div>
                        <div className="text-sm text-gray-300 bg-gray-900 p-3 rounded border border-gray-600">{testCase.explanation}</div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Constraints */}
      {problem.constraints && (
        <div className="bg-gray-800 p-4 rounded-lg border border-gray-600">
          <h3 className="text-lg font-semibold text-white mb-2">Constraints</h3>
          <div className="text-gray-200 leading-relaxed whitespace-pre-wrap bg-gray-900 p-3 rounded border border-gray-600">{problem.constraints}</div>
        </div>
      )}
    </div>
  );
};

export default ProblemDetailsView;
