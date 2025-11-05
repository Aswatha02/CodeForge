import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  }
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const adminAPI = {
  // Dashboard
  getStats: () => api.get('/admin/stats'),
  getActivity: () => api.get('/admin/activity'),
  
  // Users
  getUsers: (filters) => api.get('/admin/users', { params: filters }),
  getUserStats: () => api.get('/admin/users/stats'),
  getUserProfile: (userId) => api.get(`/admin/users/${userId}/profile`),
  updateUserRole: (userId, role) => api.put(`/admin/users/${userId}/role`, { role }),
  updateUserStatus: (userId, statusData) => api.put(`/admin/users/${userId}/status`, statusData),
  resetUserProgress: (userId) => api.post(`/admin/users/${userId}/reset-progress`),
  updateUserDetails: (userId, userData) => api.put(`/admin/users/${userId}/details`, userData),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  
  // Problems
  getProblems: (filters) => api.get('/admin/problems', { params: filters }),
  getProblem: (problemId) => api.get(`/admin/problems/${problemId}/details`),
  createProblem: (problemData) => api.post('/admin/problems', problemData),
  updateProblem: (problemId, problemData) => api.put(`/admin/problems/${problemId}`, problemData),
  deleteProblem: (problemId) => api.delete(`/admin/problems/${problemId}`),
  
  // Categories
  getCategories: () => api.get('/admin/categories'),
  createCategory: (categoryData) => api.post('/admin/categories', categoryData),
  updateCategory: (categoryId, categoryData) => api.put(`/admin/categories/${categoryId}`, categoryData),
  deleteCategory: (categoryId) => api.delete(`/admin/categories/${categoryId}`),
  
  // Submissions
  getSubmissions: (filters) => api.get('/admin/submissions', { params: filters }),
  rerunSubmission: (submissionId) => api.post(`/admin/submissions/${submissionId}/rerun`),
  
  // Contests
  getContests: () => api.get('/contests'),
  createContest: (contestData) => api.post('/contests', contestData),
  updateContest: (contestId, contestData) => api.put(`/contests/${contestId}`, contestData),
  deleteContest: (contestId) => api.delete(`/contests/${contestId}`),

  // Test case management
  addTestCase: (problemId, testCaseData) => api.post(`/admin/problems/${problemId}/test-cases`, testCaseData),
  updateTestCase: (problemId, testCaseId, testCaseData) => api.put(`/admin/problems/${problemId}/test-cases/${testCaseId}`, testCaseData),
  deleteTestCase: (problemId, testCaseId) => api.delete(`/admin/problems/${problemId}/test-cases/${testCaseId}`),
  getTestCases: (problemId) => api.get(`/admin/problems/${problemId}/test-cases`),
  
  // Code template management
  updateCodeTemplate: (problemId, language, templateData) => api.put(`/admin/problems/${problemId}/templates/${language}`, templateData),
  getCodeTemplates: (problemId) => api.get(`/admin/problems/${problemId}/templates`),
  
  // Bulk operations
  bulkUpdateTestCases: (problemId, operations) => api.post(`/admin/problems/${problemId}/test-cases/bulk`, operations),
  
  // Validation
  validateProblem: (problemData) => api.post('/admin/problems/validate', problemData),
  
  // Generic get for any endpoint
  get: (url) => api.get(url),
};

export const contestAPI = {
  // Contest listing and details
  getAllContests: (status) => api.get('/contests', { params: { status } }),
  getContest: (contestId) => api.get(`/contests/${contestId}`),
  
  // Contest CRUD (admin/problem setter)
  createContest: (contestData) => api.post('/contests', contestData),
  updateContest: (contestId, contestData) => api.put(`/contests/${contestId}`, contestData),
  deleteContest: (contestId) => api.delete(`/contests/${contestId}`),
  
  // Contest participation
  joinContest: (contestId) => api.post(`/contests/${contestId}/join`),
  leaveContest: (contestId) => api.delete(`/contests/${contestId}/leave`),
  checkRegistration: (contestId) => api.get(`/contests/${contestId}/registration-status`),
  
  // Contest dashboard
  getContestDashboard: (contestId) => api.get(`/contests/${contestId}/dashboard`),
  
  // Leaderboard
  getContestLeaderboard: (contestId) => api.get(`/contests/${contestId}/leaderboard`),
  
  // Participants (admin only)
  getContestParticipants: (contestId) => api.get(`/contests/${contestId}/participants`),
  
  // Contest problems
  getContestProblems: (contestId) => api.get(`/contests/${contestId}/problems`),
  addProblemToContest: (contestId, problemId, points) => 
    api.post(`/contests/${contestId}/problems/${problemId}`, null, { params: { points } }),
  
  // Contest control (admin)
  startContest: (contestId) => api.post(`/contests/${contestId}/start`),
  endContest: (contestId) => api.post(`/contests/${contestId}/end`),
};

export const userAPI = {

  //problems
  getProblems: () => api.get('/problems'),
  getProblemById: (id) => api.get(`/problems/${id}`),

  // User profile and stats
  getUserProfile: (userId) => api.get(`/users/${userId}`),
  updateUserProfile: (userId, data) => api.put(`/users/${userId}`, data),
  getUserStats: () => api.get('/users/me/stats'),

  // User submissions
  submitSolution: (problemId, submissionData) => api.post(`/problems/${problemId}/submissions`, submissionData),
  submitContestSolution: (contestId, problemId, submissionData) => api.post(`/contests/${contestId}/problems/${problemId}/submissions`, submissionData),
  getUserSubmissions: (filters) => api.get('/users/me/submissions', { params: filters }),
  getUserSubmissionsByProblem: (problemId) => api.get(`/users/me/problems/${problemId}/submissions`),
  getSubmissionsByProblem: (problemId) => api.get(`/problems/${problemId}/submissions`),
  getContestSubmissions: (contestId) => api.get(`/contests/${contestId}/submissions/me`),
  getSubmissionById: (id) => api.get(`/submissions/${id}`),

  // User contests
  getUserContests: () => api.get('/users/me/contests'),
  getActiveContest: () => api.get('/users/me/active-contest'),
  getContests: () => api.get('/contests'),
  getContestById: (id) => api.get(`/contests/${id}`),
  joinContest: (contestId) => api.post(`/contests/${contestId}/join`),

  // User progress
  getUserProgress: () => api.get('/users/me/progress'),
  updateUserProgress: (progressData) => api.put('/users/me/progress', progressData),

  // Leaderboard
  getLeaderboard: () => api.get('/leaderboard'),
  getContestLeaderboard: (contestId) => api.get(`/contests/${contestId}/leaderboard`),

  // Code Execution
  runCode: (executionData) => api.post('/execute', executionData),
  debugCode: (debugData) => api.post('/debug', debugData),

  // Categories
  getCategories: () => api.get('/categories')
};

export const problemAPI = {
  // Problem listing and details
  getAllProblems: (filters) => api.get('/problems', { params: filters }),
  getProblem: (problemId) => api.get(`/problems/${problemId}`),
  getProblemBySlug: (slug) => api.get(`/problems/slug/${slug}`),

  // Problem search and filtering
  searchProblems: (params) => api.get('/problems/search', { params }),
  getProblemsByCategory: (categoryId) => api.get(`/problems/category/${categoryId}`),
  getProblemsByDifficulty: (difficulty) => api.get(`/problems/difficulty/${difficulty}`),

  // Problem submissions
  submitProblem: (problemId, submissionData) => api.post(`/problems/${problemId}/submissions`, submissionData),

  // Code templates
  getCodeTemplates: (problemId) => api.get(`/problems/${problemId}/templates`),
};

export const submissionAPI = {
  // Submission management
  getSubmissionsByUserAndProblem: (userId, problemId) => api.get(`/users/${userId}/problems/${problemId}/submissions`),
  getSubmissionsByContestAndUser: (contestId, userId) => api.get(`/contests/${contestId}/users/${userId}/submissions`),
};

export const executionAPI = {
  // Code execution
  runCode: (executionData) => api.post('/execute', executionData),
  // Fix the submission endpoint
  submitSolution: (problemId, submissionData) => api.post(`/problems/${problemId}/submissions`, submissionData),
};

export default api;
