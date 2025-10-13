// src/api.js
const BASE = "http://localhost:8080/api";

export function saveUser(user) {
  localStorage.setItem("codeforge_user", JSON.stringify(user));
}
export function getUser() {
  const raw = localStorage.getItem("codeforge_user");
  return raw ? JSON.parse(raw) : null;
}

async function request(path, opts = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(opts.headers || {})
    },
    ...opts,
  });

  const ct = res.headers.get("content-type") || "";
  const isJson = ct.includes("application/json");

  if (isJson) {
    const data = await res.json();
    if (!res.ok) throw { status: res.status, data };
    return data;
  } else {
    // not JSON (could be HTML error page) -> return text so caller can show it
    const text = await res.text();
    if (!res.ok) throw { status: res.status, data: text };
    return text;
  }
}

/* User */
export const registerUser = (payload) =>
  request("/users/register", { method: "POST", body: JSON.stringify(payload) });

export const loginUser = (usernameOrEmail, password) =>
  request(`/users/login?usernameOrEmail=${encodeURIComponent(usernameOrEmail)}&password=${encodeURIComponent(password)}`, { method: "POST" });

export const getUserProgress = (userId) => request(`/users/${userId}/progress`);

/* Problems */
export const createProblem = (problem, creatorId) =>
  request(`/problems?creatorId=${creatorId}`, { method: "POST", body: JSON.stringify(problem) });

export const getProblem = (id) => request(`/problems/${id}`);

export const listProblems = () => request(`/problems`);

/* Categories */
export const createCategory = (category) =>
  request(`/categories`, { method: "POST", body: JSON.stringify(category) });

export const listCategories = () => request(`/categories`);
