import { useState } from "react"

export default function LoginForm({ onSwitchToRegister, onLogin }) {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    rememberMe: false,
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const validateForm = () => {
    const newErrors = {}
    if (!formData.email.trim()) {
      newErrors.email = "Email or username is required"
    }
    if (!formData.password) {
      newErrors.password = "Password is required"
    } else if (formData.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters"
    }
    return newErrors
  }

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }))
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }))
    }
  }

  const handleSubmit = async (e) => {
  e.preventDefault()
  const newErrors = validateForm()

  if (Object.keys(newErrors).length > 0) {
    setErrors(newErrors)
    return
  }

  setLoading(true)
  try {
    const response = await fetch('http://localhost:8080/api/users/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        usernameOrEmail: formData.email,
        password: formData.password
      }),
    })

    const responseData = await response.json()

    if (response.ok) {
      const userData = responseData.user
      const token = responseData.token

      console.log('✅ Login successful! User data:', userData)
      console.log('🔑 JWT Token received:', token ? 'Present' : 'Missing')

      // Enhanced admin detection
      const isAdmin =
        userData.username === 'admin' ||
        userData.email === 'admin@codeforge.com' ||
        userData.role === 'ADMIN' ||
        (userData.role && userData.role.toString().toUpperCase() === 'ADMIN')

      const userRole = isAdmin ? 'admin' : 'user'

      console.log('👤 User role detected:', userRole, 'isAdmin:', isAdmin)

      // Store session data with real JWT token
      localStorage.setItem("token", token)
      localStorage.setItem("userRole", userRole)
      localStorage.setItem("userId", userData.id)
      localStorage.setItem("username", userData.username)
      localStorage.setItem("email", userData.email)
      localStorage.setItem("userProfile", JSON.stringify({ ...userData, role: userRole }))

      console.log('🔄 Calling onLogin with role:', userRole)

      // Pass user data to parent component for routing
      onLogin({ ...userData, role: userRole })

    } else {
      setErrors({ submit: responseData.error || "Login failed" })
    }
  } catch (error) {
    console.error('Login error:', error)
    setErrors({ submit: "Login failed. Please check your connection and try again." })
  } finally {
    setLoading(false)
  }
}

  return (
    <div className="p-8 bg-surface rounded-lg shadow-lg border border-border">
      <div className="text-center mb-2">
        <h1 className="text-3xl font-bold text-primary mb-2">CodeForge</h1>
        <p className="text-text-muted">Practice coding, master interviews</p>
      </div>

      <h2 className="text-2xl font-bold text-text mb-6 text-center">Sign In</h2>

      {errors.submit && (
        <div className="mb-4 p-3 bg-error/10 border border-error rounded-lg text-error text-sm">
          {errors.submit}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="space-y-4 mb-6">
          <div>
            <label className="block text-sm font-medium text-text mb-2">
              Email or Username
            </label>
            <input
              type="text"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter your email or username"
              className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.email ? 'border-error' : 'border-border'
              } text-text placeholder-text-muted`}
            />
            {errors.email && (
              <p className="text-error text-sm mt-1">{errors.email}</p>
            )}
          </div>

          <div className="relative">
            <label className="block text-sm font-medium text-text mb-2">
              Password
            </label>
            <input
              type={showPassword ? "text" : "password"}
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.password ? 'border-error' : 'border-border'
              } text-text placeholder-text-muted pr-10`}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-9 text-text-muted hover:text-text transition-colors"
            >
              {showPassword ? (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-4.803m5.596-3.856a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              )}
            </button>
            {errors.password && (
              <p className="text-error text-sm mt-1">{errors.password}</p>
            )}
          </div>

          <div className="flex items-center justify-between">
            <label className="flex items-center">
              <input
                type="checkbox"
                name="rememberMe"
                checked={formData.rememberMe}
                onChange={handleChange}
                className="rounded border-border bg-surface text-primary focus:ring-primary focus:ring-2"
              />
              <span className="ml-2 text-sm text-text">Remember me</span>
            </label>
            <button type="button" className="text-primary hover:text-primary-light text-sm font-medium transition-colors">
              Forgot password?
            </button>
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-primary hover:bg-primary-dark disabled:bg-primary-light text-white py-3 px-4 rounded-md font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:ring-offset-surface mb-4"
        >
          {loading ? (
            <span className="flex items-center justify-center">
              <svg
                className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
              Signing in...
            </span>
          ) : (
            "Sign In"
          )}
        </button>
      </form>

      <div className="text-center">
        <p className="text-text-muted text-sm">
          Don't have an account?{" "}
          <button
            type="button"
            onClick={onSwitchToRegister}
            className="text-primary hover:text-primary-light font-medium transition-colors"
          >
            Create one
          </button>
        </p>
        
        {/* Admin hint for testing */}
        <div className="mt-4 p-2 bg-surface-light rounded-lg border border-border">
          <p className="text-xs text-text-muted">
            <strong>Test Admin:</strong> Use an admin account from your database
          </p>
        </div>
      </div>
    </div>
  )
}