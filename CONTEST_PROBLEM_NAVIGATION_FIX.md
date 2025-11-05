# Contest Problem Navigation Fix

## Issue
When clicking on problems in contests, the code editor was showing empty/broken because the contest was trying to render problems inline instead of reusing the existing problem page infrastructure.

## Solution
Instead of duplicating the problem viewing logic, we now redirect to the existing problem page when a user clicks on a problem in a contest, passing the contest context along.

## Changes Made

### 1. ContestDashboard.jsx
**Changes:**
- Removed `CodeEditor` import and `selectedProblem` state
- Added `onProblemSelect` prop to pass problem selection to parent
- Updated `handleProblemClick` to call parent callback with problem and contestId
- Removed inline CodeEditor rendering

**Before:**
```javascript
const [selectedProblem, setSelectedProblem] = useState(null);

if (selectedProblem) {
  return <CodeEditor problem={selectedProblem} onBack={...} contestId={contestId} />;
}
```

**After:**
```javascript
const handleProblemClick = (problem) => {
  if (onProblemSelect) {
    onProblemSelect(problem, contestId);
  }
};
```

### 2. UserDashboard.jsx
**Changes:**
- Added `problemContestId` state to track contest context
- Updated `ContestDashboard` component to pass `onProblemSelect` handler
- Modified `CodeEditor` rendering to include `contestId` prop
- Clear contest context when navigating back from problem

**Key Addition:**
```javascript
const [problemContestId, setProblemContestId] = useState(null);

// In ContestDashboard rendering:
<ContestDashboard 
  contestId={selectedContestId}
  onBack={...}
  onProblemSelect={(problem, contestId) => {
    setSelectedProblem(problem);
    setProblemContestId(contestId);
  }}
/>

// In CodeEditor rendering:
<CodeEditor
  problem={selectedProblem}
  onBack={() => {
    setSelectedProblem(null);
    setProblemContestId(null);
  }}
  contestId={problemContestId}
/>
```

## Benefits

✅ **No Duplication**: Reuses existing problem page infrastructure  
✅ **Consistent Experience**: Same code editor for both regular problems and contest problems  
✅ **Contest Context Preserved**: Submissions are properly tracked to contests  
✅ **Cleaner Code**: Removed unnecessary state and rendering logic  
✅ **Better Maintainability**: Single source of truth for problem viewing  

## How It Works

1. User clicks on a problem in contest dashboard
2. `ContestDashboard` calls `onProblemSelect(problem, contestId)`
3. `UserDashboard` sets both `selectedProblem` and `problemContestId`
4. `CodeEditor` renders with the problem and contest context
5. When user submits, the `contestId` is passed to the submission API
6. User clicks back, both states are cleared, returns to contest dashboard

## Testing

1. Navigate to Contests tab
2. Enter a contest
3. Click on any problem
4. Verify:
   - Problem loads correctly with all parameters
   - Code editor shows proper function signature
   - Can run code and submit
   - Submissions are tracked to the contest
   - Back button returns to contest dashboard

## Files Modified

1. `codeforge-frontend/src/components/ContestDashboard.jsx`
2. `codeforge-frontend/src/components/UserDashboard.jsx`

## Date
November 5, 2025
