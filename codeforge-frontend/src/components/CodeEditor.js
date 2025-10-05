import React, { useState } from 'react';

export default function CodeEditor({ onCodeSubmit, problemId, userId }) {
  const [code, setCode] = useState(`// Write your solution here
function solve(input) {
    // Your code here
    return input;
}`);
  const [language, setLanguage] = useState('javascript');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!code.trim()) {
      alert('Please write some code before submitting');
      return;
    }

    setIsSubmitting(true);
    try {
      if (onCodeSubmit) {
        await onCodeSubmit({
          problemId,
          userId,
          code,
          language
        });
      }
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 2000));
    } catch (error) {
      console.error('Submission error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const languageTemplates = {
    javascript: `function solve(input) {
    // Your code here
    return input;
}`,
    python: `def solve(input):
    # Your code here
    return input`,
    java: `public class Solution {
    public Object solve(Object input) {
        // Your code here
        return input;
    }
}`,
    cpp: `#include <iostream>
#include <vector>
using namespace std;

class Solution {
public:
    vector<int> solve(vector<int>& input) {
        // Your code here
        return input;
    }
};`
  };

  const handleLanguageChange = (newLanguage) => {
    setLanguage(newLanguage);
    setCode(languageTemplates[newLanguage] || '// Write your solution here');
  };

  return (
    <div className="code-editor">
      <div className="code-editor-header">
        <select 
          value={language} 
          onChange={(e) => handleLanguageChange(e.target.value)}
          style={{ 
            padding: '6px 12px',
            border: '1px solid #3c3c3c',
            borderRadius: '4px',
            background: '#1e1e1e',
            color: '#d4d4d4',
            fontSize: '14px'
          }}
        >
          <option value="javascript">JavaScript</option>
          <option value="python">Python</option>
          <option value="java">Java</option>
          <option value="cpp">C++</option>
        </select>
        
        <button 
          onClick={handleSubmit}
          disabled={isSubmitting}
          style={{ 
            padding: '8px 16px', 
            background: isSubmitting ? '#6c757d' : '#28a745', 
            color: 'white', 
            border: 'none',
            borderRadius: '4px',
            cursor: isSubmitting ? 'not-allowed' : 'pointer',
            fontSize: '14px',
            fontWeight: '500'
          }}
        >
          {isSubmitting ? (
            <>
              <div className="loading-spinner" style={{ 
                width: '16px', 
                height: '16px', 
                display: 'inline-block',
                marginRight: '8px'
              }}></div>
              Running...
            </>
          ) : (
            'Submit Solution'
          )}
        </button>
      </div>
      
      <div className="code-editor-content">
        <textarea
          value={code}
          onChange={(e) => setCode(e.target.value)}
          className="code-textarea"
          spellCheck="false"
        />
      </div>
    </div>
  );
}