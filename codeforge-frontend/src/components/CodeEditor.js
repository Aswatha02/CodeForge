import React, { useState, useEffect } from 'react';

export default function CodeEditor({ onCodeSubmit, problemId, userId, problemData }) {
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('javascript');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [availableLanguages, setAvailableLanguages] = useState(['javascript', 'python', 'java', 'cpp']);

  // Language mappings
  const languageMap = {
    'JAVASCRIPT': 'javascript',
    'PYTHON': 'python', 
    'JAVA': 'java',
    'CPP': 'cpp'
  };

  const reverseLanguageMap = {
    'javascript': 'JAVASCRIPT',
    'python': 'PYTHON',
    'java': 'JAVA', 
    'cpp': 'CPP'
  };

  // Get the template from backend codeTemplates if available
  const getTemplateFromBackend = (lang) => {
    if (!problemData?.codeTemplates) {
      console.log("❌ No codeTemplates in problemData");
      return null;
    }
    
    console.log("🔍 Available codeTemplates:", problemData.codeTemplates);
    
    const template = problemData.codeTemplates.find(t => {
      const templateLang = languageMap[t.language] || t.language.toLowerCase();
      console.log(`🔍 Comparing: ${templateLang} with ${lang}`);
      return templateLang === lang;
    });
    
    if (template) {
      console.log("✅ Found template:", template);
      console.log("📝 Template content:", template.templateCode);
      return template.templateCode;
    } else {
      console.log("❌ No template found for language:", lang);
      return null;
    }
  };

  // Initialize code template
  useEffect(() => {
    console.log("🔄 INITIALIZING CODE EDITOR");
    console.log("📦 Full problemData:", problemData);
    
    if (problemData?.codeTemplates) {
      console.log("🔍 Code templates found:", problemData.codeTemplates.length);
      problemData.codeTemplates.forEach((t, i) => {
        console.log(`🔍 Template ${i}:`, {
          language: t.language,
          templateCode: t.templateCode,
          id: t.id
        });
      });
    } else {
      console.log("❌ NO CODE TEMPLATES IN PROBLEM DATA");
    }

    // Set available languages
    if (problemData?.codeTemplates && problemData.codeTemplates.length > 0) {
      const languages = problemData.codeTemplates.map(template => 
        languageMap[template.language] || template.language.toLowerCase()
      );
      console.log("✅ Available languages:", languages);
      setAvailableLanguages(languages);
      
      // Set default language to first available
      if (languages.length > 0) {
        setLanguage(languages[0]);
      }
    }

    // Set the code template
    const backendTemplate = getTemplateFromBackend(language);
    
    if (backendTemplate) {
      console.log("🎯 SETTING TEMPLATE FROM BACKEND");
      setCode(backendTemplate);
    } else {
      console.log("⚠️ Using fallback template");
      // Fallback template
      setCode(`// Write your ${language} solution here\n// No template available for this problem`);
    }
  }, [problemData, language]);

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
          language: reverseLanguageMap[language] || language.toUpperCase()
        });
      }
    } catch (error) {
      console.error('Submission error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleLanguageChange = (newLanguage) => {
    console.log("🔄 Changing language to:", newLanguage);
    
    const backendTemplate = getTemplateFromBackend(newLanguage);
    
    if (backendTemplate) {
      setCode(backendTemplate);
    } else {
      setCode(`// Write your ${newLanguage} solution here\n// No template available`);
    }
    
    setLanguage(newLanguage);
  };

  return (
    <div className="code-editor">
      <div className="code-editor-header" style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '12px 16px',
        borderBottom: '1px solid #e8e8e8',
        background: 'white'
      }}>
        <select 
          value={language} 
          onChange={(e) => handleLanguageChange(e.target.value)}
          style={{ 
            padding: '6px 12px',
            border: '1px solid #d9d9d9',
            borderRadius: '4px',
            background: 'white',
            color: '#333',
            fontSize: '14px',
            minWidth: '120px'
          }}
        >
          {availableLanguages.map(lang => (
            <option key={lang} value={lang}>
              {lang.charAt(0).toUpperCase() + lang.slice(1)}
            </option>
          ))}
        </select>
        
        <button 
          onClick={handleSubmit}
          disabled={isSubmitting}
          style={{ 
            padding: '8px 20px', 
            background: isSubmitting ? '#6c757d' : '#007bff', 
            color: 'white', 
            border: 'none',
            borderRadius: '4px',
            cursor: isSubmitting ? 'not-allowed' : 'pointer',
            fontSize: '14px',
            fontWeight: '500',
            minWidth: '140px'
          }}
        >
          {isSubmitting ? (
            <>
              <div className="loading-spinner" style={{ 
                width: '16px', 
                height: '16px', 
                display: 'inline-block',
                marginRight: '8px',
                border: '2px solid transparent',
                borderTop: '2px solid white',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite'
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
          style={{
            width: '100%',
            height: '400px',
            background: '#1e1e1e',
            color: '#d4d4d4',
            border: 'none',
            padding: '16px',
            fontFamily: '"Fira Code", "Cascadia Code", monospace',
            fontSize: '14px',
            lineHeight: '1.5',
            resize: 'vertical',
            outline: 'none'
          }}
        />
      </div>
    </div>
  );
}