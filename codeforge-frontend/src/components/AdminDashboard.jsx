import { useState, useEffect } from "react"
import { adminAPI } from "../services/api"

export default function AdminDashboard({ onLogout }) {
  const [stats, setStats] = useState({
    totalProblems: 0,
    totalUsers: 0,
    totalSubmissions: 0,
    activeContests: 0,
    systemUptime: "99.9%",
    dailyActiveUsers: 0
  })
  
  const [recentActivity, setRecentActivity] = useState([])
  const [activeTab, setActiveTab] = useState("dashboard")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  // User Management State
  const [users, setUsers] = useState([])
  const [userFilters, setUserFilters] = useState({
    search: "",
    role: ""
  })

  // Problem Management State
  const [problems, setProblems] = useState([])
  const [problemFilters, setProblemFilters] = useState({
    difficulty: "",
    category: "",
    status: ""
  })

  // Contest Management State
  const [contests, setContests] = useState([])

  // Submission Management State
  const [submissions, setSubmissions] = useState([])
  const [submissionFilters, setSubmissionFilters] = useState({
    user: "",
    problem: "",
    status: "",
    language: ""
  })

  // Category Management State
  const [categories, setCategories] = useState([])
  const [newCategory, setNewCategory] = useState({ name: "", description: "", color: "#3b82f6" })

  // Problem Edit State
  const [editingProblem, setEditingProblem] = useState(null)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editFormData, setEditFormData] = useState({
    title: "",
    slug: "",
    description: "",
    inputFormat: "",
    outputFormat: "",
    difficulty: "",
    timeLimitMs: "",
    memoryLimitMb: "",
    functionName: "",
    parameters: "",
    returnType: "",
    status: "",
    categoryIds: []
  })

  // Problem Create State
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [createFormData, setCreateFormData] = useState({
    title: "",
    slug: "",
    description: "",
    inputFormat: "",
    outputFormat: "",
    difficulty: "EASY",
    timeLimitMs: "",
    memoryLimitMb: "",
    functionName: "",
    parameters: "",
    returnType: "",
    status: "DRAFT",
    categoryIds: [],
    supportedLanguages: ["JAVA", "PYTHON", "JAVASCRIPT"],
    codeTemplates: {
      JAVA: `public class Solution {
    public {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here
        
    }
}`,
      PYTHON: `class Solution:
    def {FUNCTION_NAME}(self{PARAMETERS}) -> {RETURN_TYPE}:
        # Write your code here
        pass`,
      JAVASCRIPT: `/**
 * @param {PARAMETERS} 
 * @return {RETURN_TYPE}
 */
var {FUNCTION_NAME} = function({PARAMETERS}) {
    // Write your code here
    
};`
    }
  })

  useEffect(() => {
    const userRole = localStorage.getItem("userRole")
    if (userRole !== "admin") {
      window.location.href = "/"
      return
    }
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    setLoading(true)
    setError("")
    try {
      const [statsResponse, activityResponse] = await Promise.all([
        adminAPI.getStats(),
        adminAPI.get('/admin/activity')
      ])
      
      setStats(statsResponse.data)
      setRecentActivity(activityResponse.data)

    } catch (error) {
      console.error("Error fetching dashboard data:", error)
      setError("Failed to load dashboard data. Please check your authentication and try again.")
      // Set empty stats instead of fallback data
      setStats({
        totalProblems: 0,
        totalUsers: 0,
        totalSubmissions: 0,
        activeContests: 0,
        systemUptime: "N/A",
        dailyActiveUsers: 0
      })
    }
    setLoading(false)
  }

  const fetchUsers = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getUsers(userFilters)
      setUsers(response.data)
    } catch (error) {
      console.error("Error fetching users:", error)
      setError("Failed to load users")
    }
    setLoading(false)
  }

  const fetchProblems = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getProblems(problemFilters)
      setProblems(response.data)
    } catch (error) {
      console.error("Error fetching problems:", error)
      setError("Failed to load problems")
    }
    setLoading(false)
  }

  const fetchSubmissions = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getSubmissions(submissionFilters)
      setSubmissions(response.data)
    } catch (error) {
      console.error("Error fetching submissions:", error)
      setError("Failed to load submissions")
    }
    setLoading(false)
  }

  const fetchCategories = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getCategories()
      setCategories(response.data)
    } catch (error) {
      console.error("Error fetching categories:", error)
      setError("Failed to load categories")
    }
    setLoading(false)
  }

  const fetchContests = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getContests()
      setContests(response.data)
    } catch (error) {
      console.error("Error fetching contests:", error)
      setError("Failed to load contests")
    }
    setLoading(false)
  }

  const handleCreateCategory = async () => {
    if (!newCategory.name.trim()) {
      setError("Category name is required")
      return
    }
    
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.createCategory(newCategory)
      setCategories([...categories, response.data])
      setNewCategory({ name: "", description: "", color: "#3b82f6" })
    } catch (error) {
      console.error("Error creating category:", error)
      setError("Failed to create category")
    }
    setLoading(false)
  }

  const handleDeleteUser = async (userId) => {
    if (!confirm("Are you sure you want to delete this user?")) return
    
    setLoading(true)
    setError("")
    try {
      await adminAPI.deleteUser(userId)
      setUsers(users.filter(user => user.id !== userId))
    } catch (error) {
      console.error("Error deleting user:", error)
      setError("Failed to delete user")
    }
    setLoading(false)
  }

  const handleChangeUserRole = async (userId, newRole) => {
    setLoading(true)
    setError("")
    try {
      await adminAPI.updateUserRole(userId, newRole)
      setUsers(users.map(user => 
        user.id === userId ? { ...user, role: newRole } : user
      ))
    } catch (error) {
      console.error("Error changing user role:", error)
      setError("Failed to update user role")
    }
    setLoading(false)
  }

  const handleRerunSubmission = async (submissionId) => {
    setLoading(true)
    setError("")
    try {
      await adminAPI.rerunSubmission(submissionId)
      fetchSubmissions()
    } catch (error) {
      console.error("Error rerunning submission:", error)
      setError("Failed to rerun submission")
    }
    setLoading(false)
  }

  const handleEditProblem = (problem) => {
    setEditingProblem(problem)
    setEditFormData({
      title: problem.title || "",
      slug: problem.slug || "",
      description: problem.description || "",
      inputFormat: problem.inputFormat || "",
      outputFormat: problem.outputFormat || "",
      difficulty: problem.difficulty || "",
      timeLimitMs: problem.timeLimitMs || "",
      memoryLimitMb: problem.memoryLimitMb || "",
      functionName: problem.functionName || "",
      parameters: problem.parameters || "",
      returnType: problem.returnType || "",
      status: problem.status || "",
      categoryIds: problem.categories ? problem.categories.map(cat => cat.id) : []
    })
    setShowEditModal(true)
  }

  const handleSaveProblemEdit = async () => {
    if (!editingProblem) return

    setLoading(true)
    setError("")
    try {
      await adminAPI.updateProblem(editingProblem.id, editFormData)
      setShowEditModal(false)
      setEditingProblem(null)
      fetchProblems() // Refresh the problems list
    } catch (error) {
      console.error("Error updating problem:", error)
      setError("Failed to update problem")
    }
    setLoading(false)
  }

  const handleDeleteProblem = async (problemId) => {
    if (!confirm("Are you sure you want to delete this problem?")) return

    setLoading(true)
    setError("")
    try {
      await adminAPI.deleteProblem(problemId)
      fetchProblems() // Refresh the problems list
    } catch (error) {
      console.error("Error deleting problem:", error)
      setError("Failed to delete problem")
    }
    setLoading(false)
  }

  const handleCreateProblem = async () => {
    if (!createFormData.title.trim()) {
      setError("Problem title is required")
      return
    }
    if (!createFormData.slug.trim()) {
      setError("Problem slug is required")
      return
    }
    if (!createFormData.description.trim()) {
      setError("Problem description is required")
      return
    }

    setLoading(true)
    setError("")
    try {
      await adminAPI.createProblem(createFormData)
      setShowCreateModal(false)
      setCreateFormData({
        title: "",
        slug: "",
        description: "",
        inputFormat: "",
        outputFormat: "",
        difficulty: "EASY",
        timeLimitMs: "",
        memoryLimitMb: "",
        functionName: "",
        parameters: "",
        returnType: "",
        status: "DRAFT",
        categoryIds: []
      })
      fetchProblems() // Refresh the problems list
    } catch (error) {
      console.error("Error creating problem:", error)
      setError("Failed to create problem")
    }
    setLoading(false)
  }

  useEffect(() => {
    if (activeTab === "users") fetchUsers()
    if (activeTab === "problems") fetchProblems()
    if (activeTab === "submissions") fetchSubmissions()
    if (activeTab === "categories") fetchCategories()
    if (activeTab === "contests") fetchContests()
  }, [activeTab])

  const ErrorAlert = () => (
    error && (
      <div className="bg-red-500/20 border border-red-500 text-red-400 px-4 py-3 rounded-md mb-4">
        {error}
      </div>
    )
  )

  const StatCard = ({ title, value, icon, color }) => (
    <div className="bg-surface rounded-lg p-6 border border-border hover:border-primary/50 transition-colors">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-text-muted text-sm">{title}</p>
          <p className="text-2xl font-bold text-text mt-2">{value}</p>
        </div>
        <div className={`p-3 rounded-full ${color} text-xl`}>
          {icon}
        </div>
      </div>
    </div>
  )

  const QuickActionButton = ({ icon, label, onClick, color = "bg-primary" }) => (
    <button
      onClick={onClick}
      className={`${color} hover:opacity-80 text-white py-3 px-4 rounded-md transition-colors text-center flex items-center justify-center space-x-2`}
    >
      <span>{icon}</span>
      <span>{label}</span>
    </button>
  )

  return (
    <div className="min-h-screen bg-background text-text">
      {/* Header */}
      <header className="bg-surface border-b border-border sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-primary">CodeForge Admin</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-text">Welcome, Admin</span>
              <button
                onClick={onLogout}
                className="bg-error hover:bg-error/80 text-white px-4 py-2 rounded-md transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <aside className="w-64 bg-surface border-r border-border min-h-screen sticky top-16">
          <nav className="p-4">
            <ul className="space-y-2">
              {[
                { id: "dashboard", label: "Dashboard", icon: "📊" },
                { id: "users", label: "User Management", icon: "👥" },
                { id: "problems", label: "Problem Management", icon: "💻" },
                { id: "categories", label: "Category Management", icon: "📂" },
                { id: "contests", label: "Contest Management", icon: "🏆" },
                { id: "submissions", label: "Submission Management", icon: "📝" }
              ].map((item) => (
                <li key={item.id}>
                  <button
                    onClick={() => setActiveTab(item.id)}
                    className={`w-full text-left px-4 py-3 rounded-md transition-colors ${
                      activeTab === item.id
                        ? "bg-primary text-white"
                        : "text-text hover:bg-surface-light"
                    }`}
                  >
                    <span className="mr-3">{item.icon}</span>
                    {item.label}
                  </button>
                </li>
              ))}
            </ul>
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          {/* Error Alert */}
          <ErrorAlert />

          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="text-text">Loading...</div>
            </div>
          )}

          {/* Dashboard Overview */}
          {activeTab === "dashboard" && !loading && (
            <div>
              <h2 className="text-3xl font-bold text-text mb-8">Admin Dashboard</h2>
              
              {/* Stats Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                <StatCard
                  title="Total Problems"
                  value={stats.totalProblems}
                  color="bg-blue-500/20 text-blue-400"
                  icon="💻"
                />
                <StatCard
                  title="Total Users"
                  value={stats.totalUsers}
                  color="bg-green-500/20 text-green-400"
                  icon="👥"
                />
                <StatCard
                  title="Total Submissions"
                  value={stats.totalSubmissions}
                  color="bg-purple-500/20 text-purple-400"
                  icon="📝"
                />
                <StatCard
                  title="Active Contests"
                  value={stats.activeContests}
                  color="bg-orange-500/20 text-orange-400"
                  icon="🏆"
                />
                <StatCard
                  title="System Uptime"
                  value={stats.systemUptime}
                  color="bg-emerald-500/20 text-emerald-400"
                  icon="⚡"
                />
                <StatCard
                  title="Daily Active Users"
                  value={stats.dailyActiveUsers}
                  color="bg-cyan-500/20 text-cyan-400"
                  icon="👤"
                />
              </div>

              {/* Quick Actions */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-8">
                <h3 className="text-xl font-bold text-text mb-4">Quick Actions</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  <QuickActionButton
                    icon="➕"
                    label="Add Problem"
                    onClick={() => setActiveTab("problems")}
                  />
                  <QuickActionButton
                    icon="👥"
                    label="Manage Users"
                    onClick={() => setActiveTab("users")}
                    color="bg-secondary"
                  />
                  <QuickActionButton
                    icon="🏆"
                    label="Create Contest"
                    onClick={() => setActiveTab("contests")}
                    color="bg-success"
                  />
                  <QuickActionButton
                    icon="📂"
                    label="Add Category"
                    onClick={() => setActiveTab("categories")}
                    color="bg-warning"
                  />
                </div>
              </div>

              {/* Recent Activity */}
              <div className="bg-surface rounded-lg p-6 border border-border">
                <h3 className="text-xl font-bold text-text mb-4">Recent Activity</h3>
                <div className="space-y-3">
                  {recentActivity.map((activity) => (
                    <div key={activity.id} className="flex items-center justify-between py-2 border-b border-border last:border-b-0">
                      <div className="flex items-center space-x-4">
                        <div className="w-2 h-2 bg-primary rounded-full"></div>
                        <span className="text-text font-medium">{activity.user}</span>
                        <span className="text-text-muted">{activity.action}</span>
                        {activity.problem && (
                          <span className="text-primary">{activity.problem}</span>
                        )}
                      </div>
                      <span className="text-text-muted text-sm">{activity.time}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* User Management */}
          {activeTab === "users" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">User Management</h2>
                <button className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Export Users
                </button>
              </div>

              {/* Filters */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-6">
                <h3 className="text-lg font-bold text-text mb-4">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <input
                    type="text"
                    placeholder="Search users..."
                    value={userFilters.search}
                    onChange={(e) => setUserFilters({...userFilters, search: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  />
                  <select
                    value={userFilters.role}
                    onChange={(e) => setUserFilters({...userFilters, role: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Roles</option>
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                    <option value="PROBLEM_SETTER">Problem Setter</option>
                  </select>

                  <button
                    onClick={fetchUsers}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Apply Filters
                  </button>
                </div>
              </div>

              {/* Users Table */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">User</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Role</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Submissions</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Join Date</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {users.map((user) => (
                      <tr key={user.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4">
                          <div>
                            <div className="font-medium text-text">{user.username}</div>
                            <div className="text-text-muted text-sm">{user.email}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <select
                            value={user.role}
                            onChange={(e) => handleChangeUserRole(user.id, e.target.value)}
                            className="bg-surface border border-border rounded px-2 py-1 text-sm"
                          >
                            <option value="USER">User</option>
                            <option value="ADMIN">Admin</option>
                            <option value="PROBLEM_SETTER">Problem Setter</option>
                          </select>
                        </td>
                        <td className="px-6 py-4 text-text">{user.submissionCount}</td>
                        <td className="px-6 py-4 text-text-muted">{user.joinDate}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button className="text-blue-400 hover:text-blue-300">Edit</button>
                            <button className="text-red-400 hover:text-red-300" onClick={() => handleDeleteUser(user.id)}>Delete</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Problem Management */}
          {activeTab === "problems" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Problem Management</h2>
                <button onClick={() => setShowCreateModal(true)} className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Create New Problem
                </button>
              </div>

              {/* Problem Filters */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-6">
                <h3 className="text-lg font-bold text-text mb-4">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <select
                    value={problemFilters.difficulty}
                    onChange={(e) => setProblemFilters({...problemFilters, difficulty: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Difficulties</option>
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                  <select
                    value={problemFilters.category}
                    onChange={(e) => setProblemFilters({...problemFilters, category: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Categories</option>
                    {categories.map(cat => (
                      <option key={cat.id} value={cat.name}>{cat.name}</option>
                    ))}
                  </select>
                  <select
                    value={problemFilters.status}
                    onChange={(e) => setProblemFilters({...problemFilters, status: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Status</option>
                    <option value="PUBLISHED">Published</option>
                    <option value="DRAFT">Draft</option>
                    <option value="ARCHIVED">Archived</option>
                  </select>
                  <button
                    onClick={fetchProblems}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Apply Filters
                  </button>
                </div>
              </div>

              {/* Problems Table */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Problem</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Difficulty</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Category</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Submissions</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Acceptance</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {problems.map((problem) => (
                      <tr key={problem.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4">
                          <div className="font-medium text-text">{problem.title}</div>
                        </td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            problem.difficulty === 'EASY' ? 'bg-green-500/20 text-green-400' :
                            problem.difficulty === 'MEDIUM' ? 'bg-yellow-500/20 text-yellow-400' :
                            'bg-red-500/20 text-red-400'
                          }`}>
                            {problem.difficulty}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text">{problem.category}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            problem.status === 'PUBLISHED' ? 'bg-green-500/20 text-green-400' :
                            problem.status === 'DRAFT' ? 'bg-yellow-500/20 text-yellow-400' :
                            'bg-gray-500/20 text-gray-400'
                          }`}>
                            {problem.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text">{problem.submissionCount}</td>
                        <td className="px-6 py-4 text-text">{problem.acceptanceRate}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button
                              className="text-blue-400 hover:text-blue-300"
                              onClick={() => handleEditProblem(problem)}
                            >
                              Edit
                            </button>
                            <button
                              className="text-red-400 hover:text-red-300"
                              onClick={() => handleDeleteProblem(problem.id)}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Category Management */}
          {activeTab === "categories" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Category Management</h2>
              </div>

              {/* Add Category Form */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-6">
                <h3 className="text-lg font-bold text-text mb-4">Add New Category</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <input
                    type="text"
                    placeholder="Category Name"
                    value={newCategory.name}
                    onChange={(e) => setNewCategory({...newCategory, name: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  />
                  <input
                    type="text"
                    placeholder="Description"
                    value={newCategory.description}
                    onChange={(e) => setNewCategory({...newCategory, description: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  />
                  <input
                    type="color"
                    value={newCategory.color}
                    onChange={(e) => setNewCategory({...newCategory, color: e.target.value})}
                    className="px-3 py-2 rounded-md h-10"
                  />
                  <button
                    onClick={handleCreateCategory}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Add Category
                  </button>
                </div>
              </div>

              {/* Categories Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {categories.map((category) => (
                  <div key={category.id} className="bg-surface rounded-lg p-6 border border-border hover:border-primary/50 transition-colors">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-xl font-bold text-text">{category.name}</h3>
                      <div 
                        className="w-6 h-6 rounded-full"
                        style={{ backgroundColor: category.color }}
                      ></div>
                    </div>
                    <p className="text-text-muted mb-4">{category.description}</p>
                    <div className="flex justify-between items-center">
                      <span className="text-text">{category.problemCount} problems</span>
                      <div className="flex space-x-2">
                        <button className="text-blue-400 hover:text-blue-300 text-sm">Edit</button>
                        <button className="text-red-400 hover:text-red-300 text-sm">Delete</button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Submission Management */}
          {activeTab === "submissions" && !loading && (
            <div>
              <h2 className="text-3xl font-bold text-text mb-6">Submission Management</h2>

              {/* Submission Filters */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-6">
                <h3 className="text-lg font-bold text-text mb-4">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <input
                    type="text"
                    placeholder="Search by user..."
                    value={submissionFilters.user}
                    onChange={(e) => setSubmissionFilters({...submissionFilters, user: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  />
                  <input
                    type="text"
                    placeholder="Search by problem..."
                    value={submissionFilters.problem}
                    onChange={(e) => setSubmissionFilters({...submissionFilters, problem: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  />
                  <select
                    value={submissionFilters.status}
                    onChange={(e) => setSubmissionFilters({...submissionFilters, status: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Status</option>
                    <option value="ACCEPTED">Accepted</option>
                    <option value="WRONG_ANSWER">Wrong Answer</option>
                    <option value="TIME_LIMIT_EXCEEDED">Time Limit Exceeded</option>
                    <option value="COMPILATION_ERROR">Compilation Error</option>
                  </select>
                  <button
                    onClick={fetchSubmissions}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Apply Filters
                  </button>
                </div>
              </div>

              {/* Submissions Table */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">User</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Problem</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Language</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Time</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Submitted At</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {submissions.map((submission) => (
                      <tr key={submission.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4 text-text-muted">#{submission.id}</td>
                        <td className="px-6 py-4 text-text">{submission.user}</td>
                        <td className="px-6 py-4 text-text">{submission.problem}</td>
                        <td className="px-6 py-4 text-text-muted">{submission.language}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            submission.status === 'ACCEPTED' ? 'bg-green-500/20 text-green-400' :
                            submission.status === 'WRONG_ANSWER' ? 'bg-red-500/20 text-red-400' :
                            submission.status === 'TIME_LIMIT_EXCEEDED' ? 'bg-orange-500/20 text-orange-400' :
                            'bg-yellow-500/20 text-yellow-400'
                          }`}>
                            {submission.status.replace('_', ' ')}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text-muted">{submission.executionTime}</td>
                        <td className="px-6 py-4 text-text-muted">{submission.submittedAt}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button className="text-blue-400 hover:text-blue-300 text-sm">View Code</button>
                            <button 
                              className="text-green-400 hover:text-green-300 text-sm"
                              onClick={() => handleRerunSubmission(submission.id)}
                            >
                              Re-run
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Contest Management */}
          {activeTab === "contests" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Contest Management</h2>
                <button className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Create New Contest
                </button>
              </div>
              
              <div className="bg-surface rounded-lg p-8 border border-border text-center">
                <div className="text-6xl mb-4">🏆</div>
                <h3 className="text-2xl font-bold text-text mb-2">Contest Management</h3>
                <p className="text-text-muted mb-4">Manage coding contests, participants, and leaderboards</p>
                <button 
                  onClick={() => setActiveTab("contests")}
                  className="bg-primary hover:bg-primary-dark text-white px-6 py-3 rounded-md"
                >
                  Create Your First Contest
                </button>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Problem Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-surface rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold text-text">Create New Problem</h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-text-muted hover:text-text"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Title</label>
                  <input
                    type="text"
                    value={createFormData.title}
                    onChange={(e) => setCreateFormData({...createFormData, title: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Slug</label>
                  <input
                    type="text"
                    value={createFormData.slug}
                    onChange={(e) => setCreateFormData({...createFormData, slug: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Description</label>
                <textarea
                  value={createFormData.description}
                  onChange={(e) => setCreateFormData({...createFormData, description: e.target.value})}
                  rows={4}
                  className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Input Format</label>
                  <textarea
                    value={createFormData.inputFormat}
                    onChange={(e) => setCreateFormData({...createFormData, inputFormat: e.target.value})}
                    rows={3}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Output Format</label>
                  <textarea
                    value={createFormData.outputFormat}
                    onChange={(e) => setCreateFormData({...createFormData, outputFormat: e.target.value})}
                    rows={3}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Difficulty</label>
                  <select
                    value={createFormData.difficulty}
                    onChange={(e) => setCreateFormData({...createFormData, difficulty: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Time Limit (ms)</label>
                  <input
                    type="number"
                    value={createFormData.timeLimitMs}
                    onChange={(e) => setCreateFormData({...createFormData, timeLimitMs: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Memory Limit (MB)</label>
                  <input
                    type="number"
                    value={createFormData.memoryLimitMb}
                    onChange={(e) => setCreateFormData({...createFormData, memoryLimitMb: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Status</label>
                  <select
                    value={createFormData.status}
                    onChange={(e) => setCreateFormData({...createFormData, status: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="DRAFT">Draft</option>
                    <option value="PUBLISHED">Published</option>
                    <option value="ARCHIVED">Archived</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Function Name</label>
                  <input
                    type="text"
                    value={createFormData.functionName}
                    onChange={(e) => setCreateFormData({...createFormData, functionName: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Parameters</label>
                  <input
                    type="text"
                    value={createFormData.parameters}
                    onChange={(e) => setCreateFormData({...createFormData, parameters: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Return Type</label>
                <input
                  type="text"
                  value={createFormData.returnType}
                  onChange={(e) => setCreateFormData({...createFormData, returnType: e.target.value})}
                  className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Supported Languages</label>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {["JAVA", "PYTHON", "JAVASCRIPT", "CPP", "C"].map((lang) => (
                    <label key={lang} className="flex items-center space-x-2 py-1">
                      <input
                        type="checkbox"
                        checked={createFormData.supportedLanguages.includes(lang)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setCreateFormData({
                              ...createFormData,
                              supportedLanguages: [...createFormData.supportedLanguages, lang]
                            })
                          } else {
                            setCreateFormData({
                              ...createFormData,
                              supportedLanguages: createFormData.supportedLanguages.filter(l => l !== lang)
                            })
                          }
                        }}
                        className="rounded"
                      />
                      <span className="text-text text-sm">{lang}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Code Templates</label>
                <div className="space-y-4">
                  {createFormData.supportedLanguages.map((lang) => (
                    <div key={lang} className="border border-border rounded-md p-4">
                      <h4 className="text-sm font-medium text-text mb-2">{lang} Template</h4>
                      <textarea
                        value={createFormData.codeTemplates[lang] || ""}
                        onChange={(e) => setCreateFormData({
                          ...createFormData,
                          codeTemplates: {
                            ...createFormData.codeTemplates,
                            [lang]: e.target.value
                          }
                        })}
                        rows={6}
                        className="w-full px-3 py-2 rounded-md bg-surface-light border border-border font-mono text-sm"
                        placeholder={`Enter ${lang} code template...`}
                      />
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Categories</label>
                <div className="max-h-32 overflow-y-auto border border-border rounded-md p-2">
                  {categories.map((category) => (
                    <label key={category.id} className="flex items-center space-x-2 py-1">
                      <input
                        type="checkbox"
                        checked={createFormData.categoryIds.includes(category.id)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setCreateFormData({
                              ...createFormData,
                              categoryIds: [...createFormData.categoryIds, category.id]
                            })
                          } else {
                            setCreateFormData({
                              ...createFormData,
                              categoryIds: createFormData.categoryIds.filter(id => id !== category.id)
                            })
                          }
                        }}
                        className="rounded"
                      />
                      <span className="text-text text-sm">{category.name}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2 text-text-muted hover:text-text transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateProblem}
                disabled={loading}
                className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md transition-colors disabled:opacity-50"
              >
                {loading ? "Creating..." : "Create Problem"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Problem Edit Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-surface rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold text-text">Edit Problem</h3>
              <button
                onClick={() => setShowEditModal(false)}
                className="text-text-muted hover:text-text"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Title</label>
                  <input
                    type="text"
                    value={editFormData.title}
                    onChange={(e) => setEditFormData({...editFormData, title: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Slug</label>
                  <input
                    type="text"
                    value={editFormData.slug}
                    onChange={(e) => setEditFormData({...editFormData, slug: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Description</label>
                <textarea
                  value={editFormData.description}
                  onChange={(e) => setEditFormData({...editFormData, description: e.target.value})}
                  rows={4}
                  className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Difficulty</label>
                  <select
                    value={editFormData.difficulty}
                    onChange={(e) => setEditFormData({...editFormData, difficulty: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Time Limit (ms)</label>
                  <input
                    type="number"
                    value={editFormData.timeLimitMs}
                    onChange={(e) => setEditFormData({...editFormData, timeLimitMs: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Memory Limit (MB)</label>
                  <input
                    type="number"
                    value={editFormData.memoryLimitMb}
                    onChange={(e) => setEditFormData({...editFormData, memoryLimitMb: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-text mb-1">Categories</label>
                <div className="max-h-32 overflow-y-auto border border-border rounded-md p-2">
                  {categories.map((category) => (
                    <label key={category.id} className="flex items-center space-x-2 py-1">
                      <input
                        type="checkbox"
                        checked={editFormData.categoryIds.includes(category.id)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setEditFormData({
                              ...editFormData,
                              categoryIds: [...editFormData.categoryIds, category.id]
                            })
                          } else {
                            setEditFormData({
                              ...editFormData,
                              categoryIds: editFormData.categoryIds.filter(id => id !== category.id)
                            })
                          }
                        }}
                        className="rounded"
                      />
                      <span className="text-text text-sm">{category.name}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setShowEditModal(false)}
                className="px-4 py-2 text-text-muted hover:text-text transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveProblemEdit}
                disabled={loading}
                className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md transition-colors disabled:opacity-50"
              >
                {loading ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
