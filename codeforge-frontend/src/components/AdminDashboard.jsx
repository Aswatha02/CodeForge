import { useState, useEffect } from "react"

import { adminAPI } from "../services/api"
import ProblemDetailsTab from "./ProblemDetailsTab"
import TestCasesTab from "./TestCasesTab"
import CodeTemplatesTab from "./CodeTemplatesTab"
import ProblemValidationTab from "./ProblemValidationTab"

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
  const [userStats, setUserStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    bannedUsers: 0,
    newUsersThisWeek: 0,
    newUsersThisMonth: 0,
    userGrowthData: []
  })
  const [userFilters, setUserFilters] = useState({
    search: "",
    role: "",
    status: "",
    sortBy: "createdAt",
    sortOrder: "desc",
    page: 0,
    size: 20
  })
  const [totalUsers, setTotalUsers] = useState(0)
  const [selectedUser, setSelectedUser] = useState(null)
  const [showUserProfile, setShowUserProfile] = useState(false)
  const [showEditUser, setShowEditUser] = useState(false)
  const [editUserData, setEditUserData] = useState({
    email: "",
    role: "",
    status: "",
    password: ""
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
  const [showContestModal, setShowContestModal] = useState(false)
  const [contestFormData, setContestFormData] = useState({
    title: "",
    description: "",
    startTime: "",
    endTime: "",
    problemIds: []
  })

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
  const [showCreateCategoryModal, setShowCreateCategoryModal] = useState(false)

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

  // Problem Create/Edit State with Tabbed Interface - FIXED INITIAL STATE
  const [showProblemModal, setShowProblemModal] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [activeProblemTab, setActiveProblemTab] = useState("details")
  const [problemFormData, setProblemFormData] = useState({
    title: "",
    slug: "",
    description: "",
    inputFormat: "",
    outputFormat: "",
    difficulty: "EASY",
    timeLimitMs: 2000,
    memoryLimitMb: 256,
    functionName: "solve",
    parameters: '[{"name": "input", "type": "any"}]',
    returnType: "any",
    status: "DRAFT",
    categoryIds: [],
    constraints: "",
    points: 100,
    tags: "",
    exampleInput: "",
    exampleOutput: "",
    isPrivate: false,
    supportedLanguages: ["JAVA", "PYTHON", "JAVASCRIPT", "C", "CPP"],
    testCases: [],
    codeTemplates: {
      JAVA: {
        visibleCode: `public class Solution {
    public {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
}`,
        hiddenCode: ""
      },
      PYTHON: {
        visibleCode: `class Solution:
    def {FUNCTION_NAME}(self{PARAMETERS}) -> {RETURN_TYPE}:
        # Write your code here
        pass`,
        hiddenCode: ""
      },
      JAVASCRIPT: {
        visibleCode: `/**
 * @param {PARAMETERS}
 * @return {RETURN_TYPE}
 */
var {FUNCTION_NAME} = function({PARAMETERS}) {
    // Write your code here

};`,
        hiddenCode: ""
      },
      CPP: {
        visibleCode: `class Solution {
public:
    {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
};`,
        hiddenCode: ""
      },
      C: {
        visibleCode: `{RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
    // Write your code here

}`,
        hiddenCode: ""
      }
    }
  })

  // Test API connectivity
  const testAPI = async () => {
    try {
      const token = localStorage.getItem('token')
      console.log('Current token:', token)
      
      const response = await adminAPI.getStats()
      console.log('API response:', response)
      setError("API test successful! Check console for details.")
    } catch (error) {
      console.error('API test failed:', error)
      console.log('Error details:', error.response?.data)
      setError(`API test failed: ${error.message}. Check console for details.`)
    }
  }

  useEffect(() => {
    const userRole = localStorage.getItem("userRole")
    const token = localStorage.getItem("token")
    
    if (userRole !== "admin" || !token) {
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
        adminAPI.getActivity()
      ])

      setStats(statsResponse.data)
      setRecentActivity(activityResponse.data)

    } catch (error) {
      console.error("Error fetching dashboard data:", error)
      
      // Check if it's an authentication error
      if (error.response?.status === 403) {
        setError("Access denied. Please check if you have admin privileges.")
      } else if (error.response?.status === 401) {
        setError("Authentication failed. Please log in again.")
        // Redirect to login
        localStorage.removeItem("token")
        localStorage.removeItem("userRole")
        window.location.href = "/login"
      } else {
        setError("Failed to load dashboard data. Please try again later.")
      }
      
      // Set empty stats
      setStats({
        totalProblems: 0,
        totalUsers: 0,
        totalSubmissions: 0,
        activeContests: 0,
        systemUptime: "N/A",
        dailyActiveUsers: 0
      })
      setRecentActivity([])
    }
    setLoading(false)
  }

  const fetchStats = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getStats()
      setStats(response.data)
    } catch (error) {
      console.error("Error fetching stats:", error)
      if (error.response?.status === 403) {
        setError("Access denied. Please check if you have admin privileges.")
      } else if (error.response?.status === 401) {
        setError("Authentication failed. Please log in again.")
      } else {
        setError("Failed to load statistics.")
      }
    }
    setLoading(false)
  }

  const fetchRecentActivity = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getActivity()
      setRecentActivity(response.data)
    } catch (error) {
      console.error("Error fetching recent activity:", error)
      if (error.response?.status === 403) {
        setError("Access denied.")
      } else {
        setError("Failed to load recent activity.")
      }
    }
    setLoading(false)
  }

  const fetchUsers = async () => {
    console.log('📥 Fetching users with filters:', userFilters)
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getUsers(userFilters)
      console.log('✅ Users fetched successfully:', response.data)
      setUsers(response.data)
      // Assuming the API returns total count in headers or response
      // For now, we'll use the length of returned data
      setTotalUsers(response.data.length)
      console.log(`📊 Total users: ${response.data.length}`)
    } catch (error) {
      console.error("❌ Error fetching users:", error)
      console.error("Error response:", error.response?.data)
      if (error.response?.status === 403) {
        setError("You don't have permission to view users.")
      } else {
        setError("Failed to load users: " + (error.response?.data?.message || error.message))
      }
      setUsers([])
    }
    setLoading(false)
  }

  const fetchUserStats = async () => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getUserStats()
      setUserStats(response.data)
    } catch (error) {
      console.error("Error fetching user stats:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to view user statistics.")
      } else {
        setError("Failed to load user statistics")
      }
    }
    setLoading(false)
  }

  const fetchUserProfile = async (userId) => {
    setLoading(true)
    setError("")
    try {
      const response = await adminAPI.getUserProfile(userId)
      setSelectedUser(response.data)
      setShowUserProfile(true)
    } catch (error) {
      console.error("Error fetching user profile:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to view user profiles.")
      } else {
        setError("Failed to load user profile")
      }
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
      if (error.response?.status === 403) {
        setError("You don't have permission to view problems.")
      } else {
        setError("Failed to load problems")
      }
      setProblems([])
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
      if (error.response?.status === 403) {
        setError("You don't have permission to view submissions.")
      } else {
        setError("Failed to load submissions")
      }
      setSubmissions([])
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
      if (error.response?.status === 403) {
        setError("You don't have permission to view categories.")
      } else {
        setError("Failed to load categories")
      }
      setCategories([])
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
      if (error.response?.status === 403) {
        setError("You don't have permission to view contests.")
      } else {
        setError("Failed to load contests")
      }
      setContests([])
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
      setShowCreateCategoryModal(false)
    } catch (error) {
      console.error("Error creating category:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to create categories.")
      } else {
        setError("Failed to create category")
      }
    }
    setLoading(false)
  }

  const handleDeleteUser = async (userId) => {
    if (!confirm("Are you sure you want to delete this user? This action cannot be undone.")) return

    setLoading(true)
    setError("")
    try {
      await adminAPI.deleteUser(userId)
      setUsers(users.filter(user => user.id !== userId))
      setTotalUsers(totalUsers - 1)
      // Refresh user stats to update the counts
      await fetchUserStats()
    } catch (error) {
      console.error("Error deleting user:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to delete users.")
      } else {
        setError("Failed to delete user")
      }
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
      // Refresh user stats to update the counts
      await fetchUserStats()
    } catch (error) {
      console.error("Error changing user role:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to change user roles.")
      } else {
        setError("Failed to update user role")
      }
    }
    setLoading(false)
  }

  const handleUpdateUserStatus = async (userId, newStatus) => {
    setLoading(true)
    setError("")
    try {
      await adminAPI.updateUserStatus(userId, { status: newStatus })
      setUsers(users.map(user =>
        user.id === userId ? { ...user, status: newStatus } : user
      ))
      // Refresh user stats to update the counts
      await fetchUserStats()
    } catch (error) {
      console.error("Error updating user status:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to update user status.")
      } else {
        setError("Failed to update user status")
      }
    }
    setLoading(false)
  }

  const handleResetUserProgress = async (userId) => {
    if (!confirm("Are you sure you want to reset this user's progress? This will delete all their submissions and reset their statistics.")) return

    setLoading(true)
    setError("")
    try {
      await adminAPI.resetUserProgress(userId)
      // Refresh user data
      fetchUsers()
    } catch (error) {
      console.error("Error resetting user progress:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to reset user progress.")
      } else {
        setError("Failed to reset user progress")
      }
    }
    setLoading(false)
  }

  const handleEditUser = (user) => {
    setEditUserData({
      email: user.email,
      role: user.role,
      status: user.status,
      password: ""
    })
    setSelectedUser(user)
    setShowEditUser(true)
  }

  const handleSaveUserEdit = async () => {
    if (!selectedUser) return

    setLoading(true)
    setError("")
    try {
      await adminAPI.updateUserDetails(selectedUser.id, editUserData)
      setUsers(users.map(user =>
        user.id === selectedUser.id ? {
          ...user,
          email: editUserData.email,
          role: editUserData.role,
          status: editUserData.status
        } : user
      ))
      setShowEditUser(false)
      setSelectedUser(null)
      // Refresh user stats to update the counts
      await fetchUserStats()
    } catch (error) {
      console.error("Error updating user:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to update user details.")
      } else {
        setError("Failed to update user details")
      }
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
      if (error.response?.status === 403) {
        setError("You don't have permission to rerun submissions.")
      } else {
        setError("Failed to rerun submission")
      }
    }
    setLoading(false)
  }

  const handleEditProblem = async (problem) => {
  setLoading(true)
  setError("")
  try {
    // Fetch full problem details from the backend
    const response = await adminAPI.getProblem(problem.id)
    const fullProblem = response.data

    // Log raw structure to confirm keys (expand in DevTools)
    console.log("=== EDIT PROBLEM DEBUG ===")
    console.log("Full problem data (JSON):", JSON.stringify(fullProblem, null, 2))
    console.log("Available keys:", Object.keys(fullProblem))
    console.log("Function Name (camel):", fullProblem.functionName)
    console.log("Parameters (camel):", fullProblem.parameters)
    console.log("Return Type (camel):", fullProblem.returnType)
    console.log("Categories:", fullProblem.categories)
    console.log("Test Cases:", fullProblem.testCases)
    console.log("Code Templates:", fullProblem.codeTemplates)
    console.log("==========================")

    setEditingProblem(fullProblem)
    setIsEditing(true)
    setActiveProblemTab("details")

    // Transform problem data to match form structure (use camelCase from API)
    const transformedData = {
      title: fullProblem.title || "",
      slug: fullProblem.slug || "",
      description: fullProblem.description || "",
      inputFormat: fullProblem.inputFormat || "",
      outputFormat: fullProblem.outputFormat || "",
      difficulty: fullProblem.difficulty || "EASY",
      timeLimitMs: fullProblem.timeLimitMs || 2000,
      memoryLimitMb: fullProblem.memoryLimitMb || 256,
      points: fullProblem.points || 100,
      
      // FIXED: Use camelCase from API response
      functionName: fullProblem.functionName || "solve",
      parameters: fullProblem.parameters || '[{"name":"input","type":"any"}]',
      returnType: fullProblem.returnType || "any",
      
      status: fullProblem.status || "DRAFT",
      // Handle categories: API returns strings, map to IDs using categories state
      categoryIds: fullProblem.categories 
        ? fullProblem.categories
            .map(catName => categories.find(cat => cat.name === catName)?.id)
            .filter(Boolean)  // Remove undefined
        : [],
      constraints: fullProblem.constraints || "",
      tags: fullProblem.tags || "",
      exampleInput: fullProblem.exampleInput || "",
      exampleOutput: fullProblem.exampleOutput || "",
      isPrivate: fullProblem.isPrivate || false,
      supportedLanguages: ["JAVA", "PYTHON", "JAVASCRIPT", "CPP", "C"],
      // Handle testCases: Already camelCase in API
      testCases: fullProblem.testCases || [],  // No mapping needed; direct use
      codeTemplates: {}
    }

    // Transform codeTemplates array to object format
    if (fullProblem.codeTemplates && Array.isArray(fullProblem.codeTemplates)) {
      fullProblem.codeTemplates.forEach(template => {
        transformedData.codeTemplates[template.language] = {
          visibleCode: template.visibleCode || "",
          hiddenCode: template.hiddenCode || ""
        }
      })
    } else {
      // Defaults if missing (use placeholders for new values)
      transformedData.codeTemplates = {
        JAVA: {
          visibleCode: `public class Solution {
    public ${transformedData.returnType} ${transformedData.functionName}(${transformedData.parameters}) {
        // Write your code here

    }
}`,
          hiddenCode: ""
        },
        PYTHON: {
          visibleCode: `class Solution:
    def ${transformedData.functionName}(self${transformedData.parameters}) -> ${transformedData.returnType}:
        # Write your code here
        pass`,
          hiddenCode: ""
        },
        JAVASCRIPT: {
          visibleCode: `/**
 * @param {${transformedData.parameters}}
 * @return {${transformedData.returnType}}
 */
var ${transformedData.functionName} = function(${transformedData.parameters}) {
    // Write your code here

};`,
          hiddenCode: ""
        },
        CPP: {
          visibleCode: `class Solution {
public:
    ${transformedData.returnType} ${transformedData.functionName}(${transformedData.parameters}) {
        // Write your code here

    }
};`,
          hiddenCode: ""
        },
        C: {
          visibleCode: `${transformedData.returnType} ${transformedData.functionName}(${transformedData.parameters}) {
    // Write your code here

}`,
          hiddenCode: ""
        }
      }
    }

    console.log("=== AFTER TRANSFORMATION ===")
    console.log("Function Name in form:", transformedData.functionName)
    console.log("Parameters in form:", transformedData.parameters)
    console.log("Return Type in form:", transformedData.returnType)
    console.log("Category IDs:", transformedData.categoryIds)
    console.log("============================")

    setProblemFormData(transformedData)
    setShowProblemModal(true)
  } catch (error) {
    console.error("Error fetching problem details:", error)
    console.log("Error response:", error.response?.data)
    if (error.response?.status === 403) {
      setError("You don't have permission to view this problem.")
    } else {
      setError("Failed to load problem details")
    }
  }
  setLoading(false)
}

  const handleSaveProblemEdit = async () => {
    if (!editingProblem) return

    setLoading(true)
    setError("")
    try {
      // Transform codeTemplates object to array format for backend
      const transformedData = {
        ...problemFormData,
        // Ensure numeric fields are numbers
        timeLimitMs: Number(problemFormData.timeLimitMs) || 2000,
        memoryLimitMb: Number(problemFormData.memoryLimitMb) || 256,
        points: Number(problemFormData.points) || 100,
        // Transform code templates
        codeTemplates: Object.entries(problemFormData.codeTemplates).map(([language, template]) => ({
          language,
          visibleCode: template.visibleCode,
          hiddenCode: template.hiddenCode
        }))
      }

      console.log("=== UPDATING PROBLEM ===")
      console.log("Problem ID:", editingProblem.id)
      console.log("Data being sent:", transformedData)
      console.log("=========================")

      await adminAPI.updateProblem(editingProblem.id, transformedData)
      setShowProblemModal(false)
      setIsEditing(false)
      setEditingProblem(null)
      fetchProblems() // Refresh the problems list
    } catch (error) {
      console.error("Error updating problem:", error)
      console.log("Error response:", error.response?.data)
      
      if (error.response?.status === 403) {
        setError("You don't have permission to update problems.")
      } else if (error.response?.data?.message) {
        setError(`Failed to update problem: ${error.response.data.message}`)
      } else {
        setError("Failed to update problem. Please check the console for details.")
      }
    }
    setLoading(false)
  }

  const handleDeleteProblem = async (problemId) => {
    console.log("=== DELETE PROBLEM DEBUG ===")
    console.log("Problem ID received:", problemId)
    console.log("Type of problemId:", typeof problemId)

    // Find the specific problem we're trying to delete
    const problemToDelete = problems.find(p => p.id === problemId)
    console.log("Problem to delete:", problemToDelete)

    if (!problemId) {
      setError("Invalid problem ID")
      return
    }

    if (!confirm(`Are you sure you want to delete problem "${problemToDelete?.title}"? This action cannot be undone.`)) {
      return
    }

    setLoading(true)
    setError("")

    try {
      console.log("Making API call with problemId:", problemId)

      // Ensure problemId is a number
      const idToSend = Number(problemId)
      if (isNaN(idToSend)) {
        throw new Error("Invalid problem ID format")
      }

      console.log("ID being sent to API:", idToSend)

      await adminAPI.deleteProblem(idToSend)

      // Immediately remove from local state for better UX
      setProblems(problems.filter(p => p.id !== problemId))

      // Show success message
      setError(`Problem "${problemToDelete?.title}" deleted successfully`)

      // Clear success message after 3 seconds
      setTimeout(() => {
        setError("")
      }, 3000)

    } catch (error) {
      console.error("Full error object:", error)
      console.log("Error config:", error.config)

      let errorMessage = "Failed to delete problem"

      if (error.response) {
        console.log("Error response data:", error.response.data)
        console.log("Error response status:", error.response.status)

        // Extract error message from response
        if (error.response.data?.message) {
          errorMessage = error.response.data.message
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.status === 403) {
          errorMessage = "You don't have permission to delete this problem"
        } else if (error.response.status === 404) {
          errorMessage = "Problem not found"
        } else {
          errorMessage = `Server error: ${error.response.status}`
        }
      } else if (error.message) {
        errorMessage = error.message
      }

      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  // FIXED: handleCreateProblem function with proper validation and debugging
  const handleCreateProblem = async () => {
    // Enhanced debug logging
    console.log("=== CREATING PROBLEM - FORM DATA ===")
    console.log("Function Name:", problemFormData.functionName)
    console.log("Parameters:", problemFormData.parameters)
    console.log("Return Type:", problemFormData.returnType)
    console.log("Time Limit:", problemFormData.timeLimitMs)
    console.log("Memory Limit:", problemFormData.memoryLimitMb)
    console.log("Full form data:", problemFormData)
    console.log("=====================================")

    // Validation
    if (!problemFormData.title.trim()) {
      setError("Problem title is required")
      return
    }
    if (!problemFormData.slug.trim()) {
      setError("Problem slug is required")
      return
    }
    if (!problemFormData.description.trim()) {
      setError("Problem description is required")
      return
    }
    if (!problemFormData.functionName.trim()) {
      setError("Function name is required")
      return
    }
    if (!problemFormData.parameters.trim()) {
      setError("Parameters are required")
      return
    }
    if (!problemFormData.returnType.trim()) {
      setError("Return type is required")
      return
    }

    setLoading(true)
    setError("")
    try {
      // Transform codeTemplates object to array format for backend
      const transformedData = {
        ...problemFormData,
        // Ensure numeric fields are numbers
        timeLimitMs: Number(problemFormData.timeLimitMs) || 2000,
        memoryLimitMb: Number(problemFormData.memoryLimitMb) || 256,
        points: Number(problemFormData.points) || 100,
        // Transform code templates
        codeTemplates: Object.entries(problemFormData.codeTemplates).map(([language, template]) => ({
          language,
          visibleCode: template.visibleCode,
          hiddenCode: template.hiddenCode
        }))
      }

      console.log("=== SENDING TO BACKEND ===")
      console.log("Transformed data:", transformedData)
      console.log("==========================")

      await adminAPI.createProblem(transformedData)
      setShowProblemModal(false)
      
      // Reset form with proper defaults
      setProblemFormData({
        title: "",
        slug: "",
        description: "",
        inputFormat: "",
        outputFormat: "",
        difficulty: "EASY",
        timeLimitMs: 2000,
        memoryLimitMb: 256,
        functionName: "solve",
        parameters: '[{"name": "input", "type": "any"}]',
        returnType: "any",
        status: "DRAFT",
        categoryIds: [],
        constraints: "",
        points: 100,
        tags: "",
        exampleInput: "",
        exampleOutput: "",
        isPrivate: false,
        supportedLanguages: ["JAVA", "PYTHON", "JAVASCRIPT", "C", "CPP"],
        testCases: [],
        codeTemplates: {
          JAVA: {
            visibleCode: `public class Solution {
    public {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
}`,
            hiddenCode: ""
          },
          PYTHON: {
            visibleCode: `class Solution:
    def {FUNCTION_NAME}(self{PARAMETERS}) -> {RETURN_TYPE}:
        # Write your code here
        pass`,
            hiddenCode: ""
          },
          JAVASCRIPT: {
            visibleCode: `/**
 * @param {PARAMETERS}
 * @return {RETURN_TYPE}
 */
var {FUNCTION_NAME} = function({PARAMETERS}) {
    // Write your code here

};`,
            hiddenCode: ""
          },
          CPP: {
            visibleCode: `class Solution {
public:
    {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
};`,
            hiddenCode: ""
          },
          C: {
            visibleCode: `{RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
    // Write your code here

}`,
            hiddenCode: ""
          }
        }
      })
      
      fetchProblems() // Refresh the problems list
    } catch (error) {
      console.error("Error creating problem:", error)
      console.log("Error response:", error.response?.data)
      
      if (error.response?.status === 403) {
        setError("You don't have permission to create problems.")
      } else if (error.response?.data?.message) {
        setError(`Failed to create problem: ${error.response.data.message}`)
      } else {
        setError("Failed to create problem. Please check the console for details.")
      }
    } finally {
      setLoading(false)
    }
  }

  const handleCreateContest = async () => {
    if (!contestFormData.title.trim()) {
      setError("Contest title is required")
      return
    }
    if (!contestFormData.description.trim()) {
      setError("Contest description is required")
      return
    }
    if (!contestFormData.startTime) {
      setError("Start time is required")
      return
    }
    if (!contestFormData.endTime) {
      setError("End time is required")
      return
    }
    if (contestFormData.problemIds.length === 0) {
      setError("At least one problem must be selected")
      return
    }

    setLoading(true)
    setError("")
    try {
      await adminAPI.createContest(contestFormData)
      setShowContestModal(false)
      setContestFormData({
        title: "",
        description: "",
        startTime: "",
        endTime: "",
        problemIds: []
      })
      fetchContests() // Refresh the contests list
    } catch (error) {
      console.error("Error creating contest:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to create contests.")
      } else {
        setError("Failed to create contest")
      }
    }
    setLoading(false)
  }

  const handleEditContest = (contest) => {
    setContestFormData({
      id: contest.id,
      title: contest.title,
      description: contest.description,
      startTime: contest.startTime,
      endTime: contest.endTime,
      problemIds: contest.problemIds || []
    })
    setShowContestModal(true)
  }

  const handleDeleteContest = async (contestId) => {
    if (!window.confirm("Are you sure you want to delete this contest? This action cannot be undone.")) {
      return
    }

    setLoading(true)
    setError("")
    try {
      await adminAPI.deleteContest(contestId)
      fetchContests() // Refresh the contests list
      alert("Contest deleted successfully!")
    } catch (error) {
      console.error("Error deleting contest:", error)
      if (error.response?.status === 403) {
        setError("You don't have permission to delete contests.")
      } else {
        setError(error.response?.data?.message || "Failed to delete contest")
      }
    }
    setLoading(false)
  }

  useEffect(() => {
    if (activeTab === "dashboard") {
      fetchStats()
      fetchRecentActivity()
      fetchUserStats()
    }
    if (activeTab === "users") {
      fetchUsers()
      fetchUserStats()
    }
    if (activeTab === "problems") {
      fetchProblems()
      fetchCategories() // Categories needed for problem creation and filtering
    }
    if (activeTab === "submissions") fetchSubmissions()
    if (activeTab === "categories") fetchCategories()
    if (activeTab === "contests") fetchContests()
  }, [activeTab])

  const ErrorAlert = () => (
    error && (
      <div className="bg-red-500/20 border border-red-500 text-red-400 px-4 py-3 rounded-md mb-4">
        {error}
        <button 
          onClick={testAPI}
          className="ml-4 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm"
        >
          Test API
        </button>
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

              {/* User Statistics */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
                <StatCard
                  title="Total Users"
                  value={userStats.totalUsers}
                  color="bg-blue-500/20 text-blue-400"
                  icon="👥"
                />
                <StatCard
                  title="Active Users"
                  value={userStats.activeUsers}
                  color="bg-green-500/20 text-green-400"
                  icon="✅"
                />
                <StatCard
                  title="Banned Users"
                  value={userStats.bannedUsers}
                  color="bg-red-500/20 text-red-400"
                  icon="🚫"
                />
                <StatCard
                  title="New This Week"
                  value={userStats.newUsersThisWeek}
                  color="bg-purple-500/20 text-purple-400"
                  icon="📈"
                />
              </div>

              {/* Filters */}
              <div className="bg-surface rounded-lg p-6 border border-border mb-6">
                <h3 className="text-lg font-bold text-text mb-4">Filters & Search</h3>
                <div className="grid grid-cols-1 md:grid-cols-6 gap-4">
                  <input
                    type="text"
                    placeholder="Search by username, email, or ID..."
                    value={userFilters.search}
                    onChange={(e) => setUserFilters({...userFilters, search: e.target.value})}
                    className="px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                  <select
                    value={userFilters.role}
                    onChange={(e) => setUserFilters({...userFilters, role: e.target.value})}
                    className="px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="">All Roles</option>
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                    <option value="PROBLEM_SETTER">Problem Setter</option>
                  </select>
                  <select
                    value={userFilters.status}
                    onChange={(e) => setUserFilters({...userFilters, status: e.target.value})}
                    className="px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="">All Status</option>
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                    <option value="BANNED">Banned</option>
                  </select>
                  <select
                    value={userFilters.sortBy}
                    onChange={(e) => setUserFilters({...userFilters, sortBy: e.target.value})}
                    className="px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="createdAt">Join Date</option>
                    <option value="username">Username</option>
                    <option value="lastLogin">Last Login</option>
                  </select>
                  <select
                    value={userFilters.sortOrder}
                    onChange={(e) => setUserFilters({...userFilters, sortOrder: e.target.value})}
                    className="px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="desc">Descending</option>
                    <option value="asc">Ascending</option>
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
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Username</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Email</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Role</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Join Date</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Last Login</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {users.map((user) => (
                      <tr key={user.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4">
                          <button
                            onClick={() => fetchUserProfile(user.id)}
                            className="text-primary hover:text-primary-dark font-medium"
                          >
                            {user.username}
                          </button>
                        </td>
                        <td className="px-6 py-4 text-text-muted">{user.email}</td>
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
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            user.status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' :
                            user.status === 'INACTIVE' ? 'bg-yellow-500/20 text-yellow-400' :
                            'bg-red-500/20 text-red-400'
                          }`}>
                            {user.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text-muted">{user.joinDate}</td>
                        <td className="px-6 py-4 text-text-muted">{user.lastLogin}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button
                              className="text-blue-400 hover:text-blue-300 text-sm"
                              onClick={() => handleEditUser(user)}
                            >
                              Edit
                            </button>

                            <button
                              className={`text-sm ${user.status === 'BANNED' ? 'text-green-400 hover:text-green-300' : 'text-red-400 hover:text-red-300'}`}
                              onClick={() => handleUpdateUserStatus(user.id, user.status === 'BANNED' ? 'ACTIVE' : 'BANNED')}
                            >
                              {user.status === 'BANNED' ? 'Unban' : 'Ban'}
                            </button>
                            <button
                              className="text-orange-400 hover:text-orange-300 text-sm"
                              onClick={() => handleResetUserProgress(user.id)}
                            >
                              Reset Progress
                            </button>
                            <button
                              className="text-red-400 hover:text-red-300 text-sm"
                              onClick={() => handleDeleteUser(user.id)}
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

              {/* Pagination */}
              <div className="flex justify-between items-center mt-6">
                <div className="text-text-muted">
                  Showing {users.length} of {totalUsers} users
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setUserFilters({...userFilters, page: Math.max(0, userFilters.page - 1)})}
                    disabled={userFilters.page === 0}
                    className="px-4 py-2 bg-surface border border-border rounded-md disabled:opacity-50"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => setUserFilters({...userFilters, page: userFilters.page + 1})}
                    disabled={users.length < userFilters.size}
                    className="px-4 py-2 bg-surface border border-border rounded-md disabled:opacity-50"
                  >
                    Next
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Problem Management */}
          {activeTab === "problems" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Problem Management</h2>
                <button onClick={() => {
                  setIsEditing(false)
                  setActiveProblemTab("details")
                  setProblemFormData({
                    title: "",
                    slug: "",
                    description: "",
                    inputFormat: "",
                    outputFormat: "",
                    difficulty: "EASY",
                    timeLimitMs: 2000,
                    memoryLimitMb: 256,
                    functionName: "solve",
                    parameters: '[{"name": "input", "type": "any"}]',
                    returnType: "any",
                    status: "DRAFT",
                    categoryIds: [],
                    constraints: "",
                    points: 100,
                    tags: "",
                    exampleInput: "",
                    exampleOutput: "",
                    isPrivate: false,
                    supportedLanguages: ["JAVA", "PYTHON", "JAVASCRIPT", "C", "CPP"],
                    testCases: [],
                    codeTemplates: {
                      JAVA: {
                        visibleCode: `public class Solution {
    public {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
}`,
                        hiddenCode: ""
                      },
                      PYTHON: {
                        visibleCode: `class Solution:
    def {FUNCTION_NAME}(self{PARAMETERS}) -> {RETURN_TYPE}:
        # Write your code here
        pass`,
                        hiddenCode: ""
                      },
                      JAVASCRIPT: {
                        visibleCode: `/**
 * @param {PARAMETERS}
 * @return {RETURN_TYPE}
 */
var {FUNCTION_NAME} = function({PARAMETERS}) {
    // Write your code here

};`,
                        hiddenCode: ""
                      },
                      CPP: {
                        visibleCode: `class Solution {
public:
    {RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
        // Write your code here

    }
};`,
                        hiddenCode: ""
                      },
                      C: {
                        visibleCode: `{RETURN_TYPE} {FUNCTION_NAME}({PARAMETERS}) {
    // Write your code here

}`,
                        hiddenCode: ""
                      }
                    }
                  })
                  setShowProblemModal(true)
                }} className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
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
                        <td className="px-6 py-4 text-text">{problem.acceptanceRate}%</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button
                              className="text-blue-400 hover:text-blue-300 text-sm"
                              onClick={() => handleEditProblem(problem)}
                            >
                              Edit
                            </button>
                            <button
                              className="text-red-400 hover:text-red-300 text-sm"
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
                <button onClick={() => setShowCreateCategoryModal(true)} className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Create New Category
                </button>
              </div>

              {/* Categories List */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Name</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Description</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Color</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Problems</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {categories.map((category) => (
                      <tr key={category.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4 font-medium text-text">{category.name}</td>
                        <td className="px-6 py-4 text-text-muted">{category.description}</td>
                        <td className="px-6 py-4">
                          <div className="flex items-center">
                            <div className="w-4 h-4 rounded-full mr-2" style={{backgroundColor: category.color}}></div>
                            {category.color}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-text">{category.problemCount}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button className="text-blue-400 hover:text-blue-300 text-sm">
                              Edit
                            </button>
                            <button className="text-red-400 hover:text-red-300 text-sm">
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

          {/* Contest Management */}
          {activeTab === "contests" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Contest Management</h2>
                <button onClick={() => setShowContestModal(true)} className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Create New Contest
                </button>
              </div>

              {/* Contests List */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Title</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Start Time</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">End Time</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Participants</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {contests.map((contest) => (
                      <tr key={contest.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4 font-medium text-text">{contest.title}</td>
                        <td className="px-6 py-4 text-text-muted">{contest.startTime}</td>
                        <td className="px-6 py-4 text-text-muted">{contest.endTime}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            contest.status === 'UPCOMING' ? 'bg-blue-500/20 text-blue-400' :
                            contest.status === 'RUNNING' ? 'bg-green-500/20 text-green-400' :
                            'bg-gray-500/20 text-gray-400'
                          }`}>
                            {contest.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text">{contest.participantCount}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button 
                              onClick={() => handleEditContest(contest)}
                              className="text-blue-400 hover:text-blue-300 text-sm"
                            >
                              Edit
                            </button>
                            <button 
                              onClick={() => handleDeleteContest(contest.id)}
                              className="text-red-400 hover:text-red-300 text-sm"
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

          {/* Submission Management */}
          {activeTab === "submissions" && !loading && (
            <div>
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-3xl font-bold text-text">Submission Management</h2>
                <button className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md">
                  Export Submissions
                </button>
              </div>

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
                    <option value="MEMORY_LIMIT_EXCEEDED">Memory Limit Exceeded</option>
                    <option value="RUNTIME_ERROR">Runtime Error</option>
                    <option value="COMPILATION_ERROR">Compilation Error</option>
                  </select>
                  <select
                    value={submissionFilters.language}
                    onChange={(e) => setSubmissionFilters({...submissionFilters, language: e.target.value})}
                    className="px-3 py-2 rounded-md"
                  >
                    <option value="">All Languages</option>
                    <option value="JAVA">Java</option>
                    <option value="PYTHON">Python</option>
                    <option value="JAVASCRIPT">JavaScript</option>
                  </select>
                </div>
              </div>

              {/* Submissions Table */}
              <div className="bg-surface rounded-lg border border-border overflow-hidden">
                <table className="w-full">
                  <thead className="bg-surface-dark border-b border-border">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">User</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Problem</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Language</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Time</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Memory</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Submitted At</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {submissions.map((submission) => (
                      <tr key={submission.id} className="hover:bg-surface-light transition-colors">
                        <td className="px-6 py-4 font-medium text-text">{submission.user}</td>
                        <td className="px-6 py-4 text-text">{submission.problem}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            submission.status === 'ACCEPTED' ? 'bg-green-500/20 text-green-400' :
                            submission.status === 'WRONG_ANSWER' ? 'bg-red-500/20 text-red-400' :
                            submission.status === 'TIME_LIMIT_EXCEEDED' ? 'bg-yellow-500/20 text-yellow-400' :
                            submission.status === 'MEMORY_LIMIT_EXCEEDED' ? 'bg-orange-500/20 text-orange-400' :
                            submission.status === 'RUNTIME_ERROR' ? 'bg-purple-500/20 text-purple-400' :
                            'bg-gray-500/20 text-gray-400'
                          }`}>
                            {submission.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-text-muted">{submission.language}</td>
                        <td className="px-6 py-4 text-text-muted">{submission.time}ms</td>
                        <td className="px-6 py-4 text-text-muted">{submission.memory}MB</td>
                        <td className="px-6 py-4 text-text-muted">{submission.submittedAt}</td>
                        <td className="px-6 py-4">
                          <div className="flex space-x-2">
                            <button
                              className="text-blue-400 hover:text-blue-300 text-sm"
                              onClick={() => handleRerunSubmission(submission.id)}
                            >
                              Rerun
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

          {/* Modals */}
          {showUserProfile && selectedUser && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-surface rounded-lg p-6 max-w-md w-full mx-4">
                <h3 className="text-xl font-bold text-text mb-4">User Profile</h3>
                <div className="space-y-3">
                  <p><strong>Username:</strong> {selectedUser.username}</p>
                  <p><strong>Email:</strong> {selectedUser.email}</p>
                  <p><strong>Role:</strong> {selectedUser.role}</p>
                  <p><strong>Status:</strong> {selectedUser.status}</p>
                  <p><strong>Join Date:</strong> {selectedUser.joinDate}</p>
                  <p><strong>Last Login:</strong> {selectedUser.lastLogin}</p>
                  <p><strong>Problems Solved:</strong> {selectedUser.problemsSolved}</p>
                  <p><strong>Total Submissions:</strong> {selectedUser.totalSubmissions}</p>
                </div>
                <div className="flex justify-end mt-6">
                  <button
                    onClick={() => setShowUserProfile(false)}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          )}

          {showEditUser && selectedUser && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-surface rounded-lg p-6 max-w-md w-full mx-4">
                <h3 className="text-xl font-bold text-text mb-4">Edit User</h3>
                <div className="space-y-4">
                  <input
                    type="email"
                    placeholder="Email"
                    value={editUserData.email}
                    onChange={(e) => setEditUserData({...editUserData, email: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                  <select
                    value={editUserData.role}
                    onChange={(e) => setEditUserData({...editUserData, role: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                    <option value="PROBLEM_SETTER">Problem Setter</option>
                  </select>
                  <select
                    value={editUserData.status}
                    onChange={(e) => setEditUserData({...editUserData, status: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                    <option value="BANNED">Banned</option>
                  </select>
                  <input
                    type="password"
                    placeholder="New Password (leave empty to keep current)"
                    value={editUserData.password}
                    onChange={(e) => setEditUserData({...editUserData, password: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    onClick={() => setShowEditUser(false)}
                    className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveUserEdit}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Save
                  </button>
                </div>
              </div>
            </div>
          )}

          {showProblemModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-surface rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                <h3 className="text-xl font-bold text-text mb-4">{isEditing ? 'Edit Problem' : 'Create New Problem'}</h3>
                <div className="mb-4">
                  <div className="flex space-x-4 border-b border-border">
                    <button
                      onClick={() => setActiveProblemTab('details')}
                      className={`py-2 px-4 ${activeProblemTab === 'details' ? 'border-b-2 border-primary text-primary' : 'text-text-muted'}`}
                    >
                      Details
                    </button>
                    <button
                      onClick={() => setActiveProblemTab('testCases')}
                      className={`py-2 px-4 ${activeProblemTab === 'testCases' ? 'border-b-2 border-primary text-primary' : 'text-text-muted'}`}
                    >
                      Test Cases
                    </button>
                    <button
                      onClick={() => setActiveProblemTab('codeTemplates')}
                      className={`py-2 px-4 ${activeProblemTab === 'codeTemplates' ? 'border-b-2 border-primary text-primary' : 'text-text-muted'}`}
                    >
                      Code Templates
                    </button>
                    <button
                      onClick={() => setActiveProblemTab('validation')}
                      className={`py-2 px-4 ${activeProblemTab === 'validation' ? 'border-b-2 border-primary text-primary' : 'text-text-muted'}`}
                    >
                      Validation
                    </button>
                  </div>
                </div>
                <div className="mb-6">
                  {activeProblemTab === 'details' && <ProblemDetailsTab formData={problemFormData} setFormData={setProblemFormData} categories={categories} />}
                  {activeProblemTab === 'testCases' && <TestCasesTab formData={problemFormData} setFormData={setProblemFormData} />}
                  {activeProblemTab === 'codeTemplates' && <CodeTemplatesTab formData={problemFormData} setFormData={setProblemFormData} />}
                  {activeProblemTab === 'validation' && <ProblemValidationTab formData={problemFormData} />}
                </div>
                <div className="flex justify-end space-x-3">
                  <button
                    onClick={() => setShowProblemModal(false)}
                    className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={isEditing ? handleSaveProblemEdit : handleCreateProblem}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    {isEditing ? 'Update' : 'Create'}
                  </button>
                </div>
              </div>
            </div>
          )}

          {showCreateCategoryModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-surface rounded-lg p-6 max-w-md w-full mx-4">
                <h3 className="text-xl font-bold text-text mb-4">Create New Category</h3>
                <div className="space-y-4">
                  <input
                    type="text"
                    placeholder="Category Name"
                    value={newCategory.name}
                    onChange={(e) => setNewCategory({...newCategory, name: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                  <textarea
                    placeholder="Description"
                    value={newCategory.description}
                    onChange={(e) => setNewCategory({...newCategory, description: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                    rows="3"
                  />
                  <div className="flex items-center space-x-2">
                    <input
                      type="color"
                      value={newCategory.color}
                      onChange={(e) => setNewCategory({...newCategory, color: e.target.value})}
                      className="w-12 h-10 rounded border border-border"
                    />
                    <span className="text-text-muted">Color</span>
                  </div>
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    onClick={() => setShowCreateCategoryModal(false)}
                    className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleCreateCategory}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Create
                  </button>
                </div>
              </div>
            </div>
          )}

          {showContestModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-surface rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                <h3 className="text-xl font-bold text-text mb-4">Create New Contest</h3>
                <div className="space-y-4">
                  <input
                    type="text"
                    placeholder="Contest Title"
                    value={contestFormData.title}
                    onChange={(e) => setContestFormData({...contestFormData, title: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                  />
                  <textarea
                    placeholder="Contest Description"
                    value={contestFormData.description}
                    onChange={(e) => setContestFormData({...contestFormData, description: e.target.value})}
                    className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                    rows="4"
                  />
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-text-muted mb-1">Start Time</label>
                      <input
                        type="datetime-local"
                        value={contestFormData.startTime}
                        onChange={(e) => setContestFormData({...contestFormData, startTime: e.target.value})}
                        className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-text-muted mb-1">End Time</label>
                      <input
                        type="datetime-local"
                        value={contestFormData.endTime}
                        onChange={(e) => setContestFormData({...contestFormData, endTime: e.target.value})}
                        className="w-full px-3 py-2 rounded-md bg-surface-light border border-border"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-text-muted mb-2">Select Problems</label>
                    <div className="bg-surface-light border border-border rounded-md p-4 max-h-48 overflow-y-auto">
                      {problems.length === 0 ? (
                        <p className="text-text-muted text-sm">No problems available. Create some problems first.</p>
                      ) : (
                        problems.map((problem) => (
                          <label key={problem.id} className="flex items-center space-x-2 mb-2">
                            <input
                              type="checkbox"
                              checked={contestFormData.problemIds.includes(problem.id)}
                              onChange={(e) => {
                                if (e.target.checked) {
                                  setContestFormData({
                                    ...contestFormData,
                                    problemIds: [...contestFormData.problemIds, problem.id]
                                  })
                                } else {
                                  setContestFormData({
                                    ...contestFormData,
                                    problemIds: contestFormData.problemIds.filter(id => id !== problem.id)
                                  })
                                }
                              }}
                              className="rounded border-border"
                            />
                            <span className="text-text text-sm">{problem.title}</span>
                            <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                              problem.difficulty === 'EASY' ? 'bg-green-500/20 text-green-400' :
                              problem.difficulty === 'MEDIUM' ? 'bg-yellow-500/20 text-yellow-400' :
                              'bg-red-500/20 text-red-400'
                            }`}>
                              {problem.difficulty}
                            </span>
                          </label>
                        ))
                      )}
                    </div>
                  </div>
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    onClick={() => setShowContestModal(false)}
                    className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleCreateContest}
                    className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md"
                  >
                    Create Contest
                  </button>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}