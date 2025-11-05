# Fix Submission Timeout Issue

## Current Problem
- SubmissionService.executeAgainstTestCases() runs each test case in separate Docker containers
- This causes overhead leading to TIME_LIMIT_EXCEEDED errors
- ExecutionService runs all test cases efficiently in single container

## Information Gathered
- SubmissionService.executeAgainstTestCases() loops through test cases and calls executeSingleTestCase() for each
- Each executeSingleTestCase() creates a new Docker container
- ExecutionService.executeCode() processes multiple test cases in single execution
- Need to adapt ExecutionService for submission context (code templates, function signatures)

## Plan
- Create new method executeSubmissionCode() in ExecutionService that:
  - Takes combined code, language, list of test cases, time/memory limits
  - Uses existing batch execution logic from executeCode()
  - Returns results for all test cases
- Modify SubmissionService.executeAgainstTestCases() to:
  - Prepare combined code using existing logic
  - Call new executeSubmissionCode() method
  - Process results and update submission status

## Dependent Files to be edited
- src/main/java/com/CodeForge/CodeForge/services/ExecutionService.java: Add executeSubmissionCode() method
- src/main/java/com/CodeForge/CodeForge/services/SubmissionService.java: Modify executeAgainstTestCases() to use batch execution

## Followup steps
- Test the fix by running submissions
- Verify time limits are properly handled for combined execution
- Ensure all test cases are executed correctly
