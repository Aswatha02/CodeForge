import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, // ADD THIS LINE
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
  getStats: () => api.get('/admin/stats'),
  getUsers: (filters) => api.get('/admin/users', { params: filters }),
  updateUserRole: (userId, role) => api.put(`/admin/users/${userId}/role`, { role }),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  getProblems: (filters) => api.get('/admin/problems', { params: filters }),
  createProblem: (problemData) => api.post('/admin/problems', problemData),
  updateProblem: (problemId, problemData) => api.put(`/admin/problems/${problemId}`, problemData),
  deleteProblem: (problemId) => api.delete(`/admin/problems/${problemId}`),
  getCategories: () => api.get('/admin/categories'),
  createCategory: (categoryData) => api.post('/admin/categories', categoryData),
  updateCategory: (categoryId, categoryData) => api.put(`/admin/categories/${categoryId}`, categoryData),
  deleteCategory: (categoryId) => api.delete(`/admin/categories/${categoryId}`),
  getSubmissions: (filters) => api.get('/admin/submissions', { params: filters }),
  rerunSubmission: (submissionId) => api.post(`/admin/submissions/${submissionId}/rerun`),
  getContests: () => api.get('/admin/contests'),
  createContest: (contestData) => api.post('/admin/contests', contestData),
  get: (url) => api.get(url),
};

export default api;