# Contest System API Documentation

## Overview
Complete contest management system with creation, registration, dashboard, leaderboard, and problem management.

## API Endpoints

### 1. Contest Management

#### Get All Contests
```
GET /api/contests?status={UPCOMING|RUNNING|COMPLETED}
```
**Response:** List of contests with participant count, problem count, registration status, and time remaining.

#### Get Contest Details
```
GET /api/contests/{id}
```
**Response:** Full contest details.

#### Create Contest
```
POST /api/contests
Authorization: Admin or Problem Setter
Body: {
  "title": "Spring Coding Challenge 2024",
  "description": "Test your coding skills",
  "startTime": "2024-12-01T10:00:00",
  "endTime": "2024-12-01T13:00:00",
  "duration": 180,
  "isPublic": true,
  "maxParticipants": 100,
  "problemIds": [1, 2, 3]  // Optional
}
```
**Response:** Created contest with ID.

#### Update Contest
```
PUT /api/contests/{id}
Authorization: Admin or Problem Setter
Body: Contest object
```
**Response:** Updated contest.

#### Delete Contest
```
DELETE /api/contests/{id}
Authorization: Admin only
```
**Response:** 200 OK

---

### 2. Contest Registration

#### Join Contest
```
POST /api/contests/{id}/join
Authorization: Any authenticated user
```
**Response:** 200 OK

#### Leave Contest
```
DELETE /api/contests/{id}/leave
Authorization: Any authenticated user
```
**Response:** 200 OK

#### Check Registration Status
```
GET /api/contests/{id}/registration-status
```
**Response:** 
```json
{
  "isRegistered": true
}
```

#### Get Participants (Admin only)
```
GET /api/contests/{id}/participants
Authorization: Admin only
```
**Response:** List of registered participants.

---

### 3. Contest Dashboard

#### Get Full Dashboard
```
GET /api/contests/{id}/dashboard
Authorization: Registered participants only
```
**Response:**
```json
{
  "contest": {
    "id": 1,
    "title": "Spring Challenge",
    "status": "RUNNING",
    "participantCount": 50,
    "problemCount": 5,
    "timeRemaining": 3600
  },
  "problems": [
    {
      "id": 1,
      "title": "Two Sum",
      "difficulty": "EASY",
      "points": 100,
      "totalSubmissions": 45,
      "acceptedSubmissions": 30,
      "solved": true,
      "userAttempts": 3
    }
  ],
  "userRank": {
    "rank": 12,
    "username": "john_doe",
    "score": 300,
    "problemsSolved": 3
  },
  "topRanks": [...],
  "stats": {
    "totalParticipants": 50,
    "totalSubmissions": 150,
    "timeElapsed": 1800,
    "timeRemaining": 3600
  }
}
```

---

### 4. Leaderboard

#### Get Contest Leaderboard
```
GET /api/contests/{id}/leaderboard
```
**Response:**
```json
[
  {
    "rank": 1,
    "userId": 5,
    "username": "alice",
    "score": 500,
    "problemsSolved": 5,
    "totalTime": 7200,
    "penalties": 0
  },
  ...
]
```

---

### 5. Contest Problems

#### Get Contest Problems
```
GET /api/contests/{id}/problems
Authorization: Registered participants only
```
**Response:** List of problems with user-specific stats (attempts, solved status).

#### Add Problem to Contest
```
POST /api/contests/{contestId}/problems/{problemId}?points=100
Authorization: Admin only
```
**Response:** 200 OK

---

### 6. Contest Control (Admin)

#### Start Contest
```
POST /api/contests/{id}/start
Authorization: Admin only
```
**Response:** 200 OK

#### End Contest
```
POST /api/contests/{id}/end
Authorization: Admin only
```
**Response:** 200 OK

---

## Contest Statuses

- **UPCOMING**: Contest not started yet, registration open
- **RUNNING**: Contest in progress, submissions accepted
- **COMPLETED**: Contest ended, results finalized
- **CANCELLED**: Contest cancelled

---

## Features

### ✅ Contest Creation
- Create contests with title, description, time range
- Set public/private visibility
- Limit maximum participants
- Add problems during creation

### ✅ Registration System
- Join/leave contests
- Check registration status
- View participant list (admin)

### ✅ Real-time Dashboard
- Live contest stats
- Problem list with solve status
- Personal rank and score
- Top 10 leaderboard
- Time remaining countdown

### ✅ Leaderboard
- Real-time ranking
- Score-based sorting
- Problems solved count
- Time penalties (future)

### ✅ Problem Management
- Add/remove problems
- Set custom points per problem
- Track submission stats
- User-specific progress

### ✅ Access Control
- Public/private contests
- Registration-based access
- Admin controls
- Problem setter permissions

---

## Usage Example

### Creating a Contest

```javascript
// 1. Create contest
POST /api/contests
{
  "title": "Weekly Challenge #42",
  "description": "Solve 5 problems in 3 hours",
  "startTime": "2024-12-10T14:00:00",
  "endTime": "2024-12-10T17:00:00",
  "duration": 180,
  "isPublic": true,
  "maxParticipants": 200,
  "problemIds": [23, 45, 67, 89, 12]
}

// 2. Users register
POST /api/contests/1/join

// 3. Admin starts contest
POST /api/contests/1/start

// 4. Users access dashboard
GET /api/contests/1/dashboard

// 5. Users solve problems
POST /api/problems/23/submissions

// 6. View leaderboard
GET /api/contests/1/leaderboard

// 7. Admin ends contest
POST /api/contests/1/end
```

---

## Frontend Integration

### Contest List Page
- Fetch: `GET /api/contests?status=UPCOMING`
- Display: Cards with title, time, participants
- Actions: Join/View buttons

### Contest Dashboard
- Fetch: `GET /api/contests/{id}/dashboard`
- Display: Timer, problems, leaderboard, stats
- Real-time updates every 30 seconds

### Leaderboard Page
- Fetch: `GET /api/contests/{id}/leaderboard`
- Display: Ranked table with scores
- Highlight current user
- Auto-refresh every 10 seconds

---

## Database Schema

### Tables Used
- `contests` - Contest details
- `contest_participants` - User registrations
- `contest_problems` - Problem assignments with points
- `submissions` - User submissions (existing)
- `problems` - Problem details (existing)
- `users` - User accounts (existing)

---

## Next Steps

1. **Frontend Development**
   - Contest list page
   - Contest dashboard
   - Leaderboard component
   - Registration UI

2. **Enhancements**
   - Email notifications
   - Contest announcements
   - Time penalties for wrong submissions
   - Partial scoring
   - Contest templates
   - Automated contest scheduling

3. **Analytics**
   - Contest statistics
   - Export results (CSV/Excel)
   - Performance graphs
   - Problem difficulty analysis
