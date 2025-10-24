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
  getContests: () => api.get('/admin/contests'),
  createContest: (contestData) => api.post('/admin/contests', contestData),
  updateContest: (contestId, contestData) => api.put(`/admin/contests/${contestId}`, contestData),
  deleteContest: (contestId) => api.delete(`/admin/contests/${contestId}`),

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
  getAllContests: () => api.get('/contests'),
  getContest: (contestId) => api.get(`/contests/${contestId}`),
  
  // Contest participation
  joinContest: (contestId) => api.post(`/contests/${contestId}/join`),
  
  // Leaderboard
  getContestLeaderboard: (contestId) => api.get(`/contests/${contestId}/leaderboard`),
  
  // Participants (admin only)
  getContestParticipants: (contestId) => api.get(`/contests/${contestId}/participants`),
  
  // Contest problems
  getContestProblems: (contestId) => api.get(`/contests/${contestId}/problems`),
  
  // User contests
  getUserContests: () => api.get('/users/me/contests'),
  getActiveContest: () => api.get('/users/me/active-contest'),

  updateContest: async (contestId, contestData) => {
    const response = await axios.put(`/api/admin/contests/${contestId}`, contestData);
    return response.data;
  },

  // Delete contest
  deleteContest: async (contestId) => {
    const response = await axios.delete(`/api/admin/contests/${contestId}`);
    return response.data;
  }
};

export default api;
