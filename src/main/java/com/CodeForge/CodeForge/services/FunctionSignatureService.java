package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.Submission;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FunctionSignatureService {
    
    public String detectFunctionName(String code, Submission.Language language) {
        if (code == null || code.trim().isEmpty()) {
            return "solve"; // default function name
        }
        
        switch (language) {
            case JAVASCRIPT:
                return detectJavaScriptFunction(code);
            case PYTHON:
                return detectPythonFunction(code);
            case JAVA:
                return detectJavaFunction(code);
            case CPP:
                return detectCppFunction(code);
            default:
                return "solve"; // default function name
        }
    }
    
    private String detectJavaScriptFunction(String code) {
        // Remove comments to avoid false matches
        String cleanCode = removeComments(code, "javascript");
        
        // Match function declarations: function functionName(...)
        Pattern pattern = Pattern.compile("function\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Match arrow functions: const/let/var functionName = (...)
        pattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:\\([^)]*\\)|\\w+)\\s*=>");
        matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Match function expressions: const/let/var functionName = function(...)
        pattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*function\\s*\\(");
        matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "solve";
    }
    
    private String detectPythonFunction(String code) {
        // Remove comments to avoid false matches
        String cleanCode = removeComments(code, "python");
        
        // Match def function_name(...)
        Pattern pattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Match class methods: def method_name(self, ...)
        pattern = Pattern.compile("class\\s+\\w+\\s*:.*?def\\s+(\\w+)\\s*\\(");
        matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "solve";
    }
    
    private String detectJavaFunction(String code) {
        // Remove comments to avoid false matches
        String cleanCode = removeComments(code, "java");

        // Match public/private/protected methods: [modifiers] returnType methodName(...)
        Pattern pattern = Pattern.compile(
            "(?:public|private|protected|static|final|synchronized|abstract|native)\\s+" + // modifiers
            "(?:\\w+(?:<[^>]*>)?\\s+)*" + // return type (may include generics)
            "(\\w+)\\s*\\(", // method name
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(cleanCode);

        // Find the first method that's not a constructor (different from class name)
        while (matcher.find()) {
            String methodName = matcher.group(1);

            // Skip constructors (methods with same name as class)
            if (!isLikelyConstructor(methodName, cleanCode)) {
                return methodName;
            }
        }

        // If no suitable method found, try simpler pattern
        pattern = Pattern.compile("\\w+(?:<[^>]*>)?\\s+(\\w+)\\s*\\(");
        matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "solve";
    }
    
    private String detectCppFunction(String code) {
        // Remove comments to avoid false matches
        String cleanCode = removeComments(code, "cpp");
        
        // Match function declarations: returnType functionName(...)
        Pattern pattern = Pattern.compile(
            "(?:\\w+\\s+)*" + // return type and modifiers
            "(\\w+)\\s*\\(", // function name
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(cleanCode);
        
        // Find the first function that's not a constructor/destructor
        while (matcher.find()) {
            String functionName = matcher.group(1);
            
            // Skip constructors/destructors and common C++ keywords
            if (!isCppSpecialMethod(functionName)) {
                return functionName;
            }
        }
        
        // Try matching class methods
        pattern = Pattern.compile("class\\s+\\w+\\s*\\{.*?(\\w+)\\s*\\(", Pattern.DOTALL);
        matcher = pattern.matcher(cleanCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "solve";
    }
    
    private boolean isLikelyConstructor(String methodName, String code) {
        // Check if this might be a constructor by looking for class definitions
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);
        if (classMatcher.find()) {
            String className = classMatcher.group(1);
            return methodName.equals(className);
        }
        return false;
    }
    
    private boolean isCppSpecialMethod(String methodName) {
        return methodName.equals("main") || 
               methodName.startsWith("~") || // destructor
               methodName.equals("operator") ||
               methodName.matches("^[A-Z].*"); // likely class name (constructor)
    }
    
    private String removeComments(String code, String language) {
        if (code == null) return "";
        
        String cleanCode = code;
        
        switch (language.toLowerCase()) {
            case "javascript":
            case "java":
            case "cpp":
                // Remove single-line comments
                cleanCode = cleanCode.replaceAll("//.*", "");
                // Remove multi-line comments
                cleanCode = cleanCode.replaceAll("/\\*.*?\\*/", "");
                break;
            case "python":
                // Remove single-line comments
                cleanCode = cleanCode.replaceAll("#.*", "");
                // Remove multi-line comments (triple quotes)
                cleanCode = cleanCode.replaceAll("(\"\"\"|''').*?\\1", "");
                break;
        }
        
        return cleanCode;
    }
    
    // Additional utility method to detect function parameters
    public String detectFunctionParameters(String code, Submission.Language language) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }
        
        String cleanCode = removeComments(code, language.toString().toLowerCase());
        
        switch (language) {
            case JAVASCRIPT:
                return detectJavaScriptParameters(cleanCode);
            case PYTHON:
                return detectPythonParameters(cleanCode);
            case JAVA:
                return detectJavaParameters(cleanCode);
            case CPP:
                return detectCppParameters(cleanCode);
            default:
                return "";
        }
    }
    
    private String detectJavaScriptParameters(String code) {
        Pattern pattern = Pattern.compile("function\\s+\\w+\\s*\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Check arrow functions
        pattern = Pattern.compile("(?:const|let|var)\\s+\\w+\\s*=\\s*\\(([^)]*)\\)\\s*=>");
        matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "";
    }
    
    private String detectPythonParameters(String code) {
        Pattern pattern = Pattern.compile("def\\s+\\w+\\s*\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    private String detectJavaParameters(String code) {
        Pattern pattern = Pattern.compile(
            "(?:public|private|protected|static|final|synchronized|abstract|native)\\s+" +
            "(?:\\w+\\s+)*" +
            "\\w+\\s*\\(([^)]*)\\)"
        );
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    private String detectCppParameters(String code) {
        Pattern pattern = Pattern.compile(
            "(?:\\w+\\s+)*" +
            "\\w+\\s*\\(([^)]*)\\)"
        );
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    // Method to validate if detected function is likely correct
    public boolean isValidFunctionName(String functionName) {
        if (functionName == null || functionName.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - function name should be alphanumeric and start with letter
        return functionName.matches("[a-zA-Z_][a-zA-Z0-9_]*") &&
               !functionName.equals("solve") && // not the default
               functionName.length() > 1; // at least 2 characters
    }
}