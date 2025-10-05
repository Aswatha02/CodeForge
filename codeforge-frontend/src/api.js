const BASE = "http://localhost:8080/api";

// Auth helper functions
export function saveUser(user) {
  localStorage.setItem("codeforge_user", JSON.stringify(user));
}

export function getUser() {
  const raw = localStorage.getItem("codeforge_user");
  return raw ? JSON.parse(raw) : null;
}

export function isAdmin() {
  const user = getUser();
  return user && user.role === 'ADMIN';
}

export function logout() {
  localStorage.removeItem("codeforge_user");
}

// API request helper
async function request(path, opts = {}) {
  const user = getUser();
  const headers = {
    "Content-Type": "application/json",
    ...(user ? { "Authorization": `Bearer ${user.token || user.id}` } : {}),
    ...(opts.headers || {})
  };

  const res = await fetch(`${BASE}${path}`, {
    headers,
    ...opts,
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  
  if (!res.ok) {
    throw { status: res.status, data };
  }
  return data;
}

/* User APIs */
export const registerUser = (payload) =>
  request("/users/register", { method: "POST", body: JSON.stringify(payload) });

export const loginUser = (usernameOrEmail, password) =>
  request(`/users/login`, { 
    method: "POST", 
    body: JSON.stringify({ usernameOrEmail, password }) 
  });

export const getUserProgress = (userId) => request(`/users/${userId}/progress`);

export const updateUserProgress = (userId, progress) =>
  request(`/users/${userId}/progress`, { method: "PUT", body: JSON.stringify(progress) });

/* Problem APIs */
// FIXED: Use correct endpoint with creatorId parameter
export const createProblem = (problemData, creatorId) =>
  request(`/problems?creatorId=${creatorId}`, { 
    method: "POST", 
    body: JSON.stringify(problemData) 
  });

export const getProblem = (id) => request(`/problems/${id}`);

export const listProblems = () => request(`/problems`);

export const getProblemsByCategory = (categoryId) => request(`/categories/${categoryId}/problems`);

export const submitSolution = (problemId, userId, code, language) =>
  request(`/problems/${problemId}/submit`, { 
    method: "POST", 
    body: JSON.stringify({ userId, code, language }) 
  });

export const getSubmissions = (userId, problemId) => {
  let url = `/submissions`;
  const params = [];
  if (userId) params.push(`userId=${userId}`);
  if (problemId) params.push(`problemId=${problemId}`);
  return request(url + (params.length ? `?${params.join('&')}` : ''));
};

/* Category APIs */
export const createCategory = (category) =>
  request(`/categories`, { method: "POST", body: JSON.stringify(category) });

export const listCategories = () => request(`/categories`);

export const getCategory = (id) => request(`/categories/${id}`);

/* Admin APIs */
// FIXED: Use correct admin endpoints that exist in your backend
export const getAllUsers = () => request(`/users/admin/users`);
export const getSystemStats = () => request(`/submissions/admin/stats`); // This might not exist yet
export const deleteProblem = (problemId) => request(`/problems/admin/${problemId}`, { method: "DELETE" });
export const deleteCategory = (categoryId) => request(`/categories/admin/${categoryId}`, { method: "DELETE" });