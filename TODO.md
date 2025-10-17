# CodeForge Submission System Fixes - TODO

## Completed Fixes ✅

### 1. SubmissionController.java
- [x] Fixed language parameter usage (changed `submissionRequest.getLanguage()` to `dto.getLanguage()`)
- [x] Fixed contest ID passing (changed `null` to `contestId` for contest submissions)

### 2. Submission.java
- [x] Added `expectedOutput` and `actualOutput` fields with proper annotations
- [x] Implemented `setExpectedOutput()` and `setActualOutput()` methods

### 3. SubmissionService.java
- [x] Removed duplicated function detection code (218-280 lines)
- [x] Enhanced error handling with try-catch blocks
- [x] Improved Docker metrics (still simulated but more realistic)

### 4. FunctionSignatureService.java
- [x] Enhanced Java function detection regex to handle generics better

### 5. Repository Fixes
- [x] Added `deleteByProblemId` method to SubmissionRepository for cascading deletes
- [x] Added `deleteByProblem` method to ContestProblemRepository for cascading deletes

### 6. Lombok Issues Fixed
- [x] Replaced @Data annotations with manual getters/setters in DTO classes (ProblemRequest, CodeTemplateRequest, TestCaseRequest)
- [x] Fixed compilation errors due to Lombok not working in this environment

### 7. Repository Query Issues Fixed
- [x] Removed invalid `deleteByProblemId` method from UserProgressRepository (UserProgress entity doesn't have a problem field)
- [x] Removed call to `userProgressRepository.deleteByProblemId()` from ProblemService
- [x] Application now starts successfully on port 8080

## Testing Status

### What has been tested:
- [x] Code compilation (mvn compile) - SUCCESS
- [x] No syntax errors in modified files
- [x] DTO classes compile correctly with manual getters/setters

### Remaining areas that require coverage:
- [ ] Submission endpoints functionality (POST /api/problems/{id}/submissions)
- [ ] Contest submission endpoints (POST /api/contests/{contestId}/problems/{problemId}/submissions)
- [ ] Function detection for different languages (JavaScript, Python, Java, C++)
- [ ] Docker execution with actual code samples
- [ ] Output comparison logic
- [ ] Contest scoring and leaderboard updates
- [ ] Error handling for invalid code, timeouts, etc.
- [ ] Problem creation and deletion with cascading deletes

## Next Steps

1. **Critical-path testing** (recommended first):
   - Test basic submission flow with simple code
   - Verify language parameter is correctly passed
   - Check contest submissions include contest ID
   - Test problem creation with new DTO structure

2. **Thorough testing** (after critical-path):
   - Test all supported languages
   - Test edge cases (invalid code, timeouts, compilation errors)
   - Test contest scoring and leaderboard updates
   - Test function detection with complex signatures
   - Test problem deletion with cascading deletes

## API Endpoints to Test

### Primary endpoints impacted:
- `POST /api/problems` - Problem creation (now uses fixed DTOs)
- `POST /api/problems/{problemId}/submissions` - Regular problem submissions
- `POST /api/contests/{contestId}/problems/{problemId}/submissions` - Contest submissions
- `DELETE /api/problems/{id}` - Problem deletion (with cascading)

### Test scenarios:
- Valid submissions in all languages
- Invalid code submissions
- Contest submissions with scoring
- Function detection accuracy
- Output comparison accuracy
- Problem creation with categories, test cases, and code templates
- Problem deletion cascading to submissions and contest problems
