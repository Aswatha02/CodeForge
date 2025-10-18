import { useState } from "react"

export default function RegisterForm({ onSwitchToLogin, onLogin }) {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const validateForm = () => {
    const newErrors = {}

    if (!formData.username.trim()) {
      newErrors.username = "Username is required"
    } else if (formData.username.length < 3) {
      newErrors.username = "Username must be at least 3 characters"
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email is required"
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email"
    }

    if (!formData.password) {
      newErrors.password = "Password is required"
    } else if (formData.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters"
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match"
    }

    return newErrors
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
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
    setErrors({})
    
    try {
      console.log('🔄 Attempting registration with:', {
        username: formData.username,
        email: formData.email,
        password: formData.password
      })

      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.username,
          email: formData.email,
          passwordHash: formData.password
        }),
      })

      console.log('📥 Response status:', response.status)
      console.log('📥 Response ok:', response.ok)
      
      let responseData
      try {
        responseData = await response.json()
        console.log('📥 Response data:', responseData)
      } catch (parseError) {
        console.error('❌ Failed to parse JSON response:', parseError)
        if (response.ok) {
          const text = await response.text()
          console.log('📥 Raw response text:', text)
          throw new Error('Server returned non-JSON response')
        } else {
          throw new Error('Failed to parse error response')
        }
      }

      if (response.ok) {
        console.log('✅ Registration successful! User data:', responseData)
        
        // Extract user data safely
        const userData = responseData
        const userId = userData.id
        const username = userData.username
        const email = userData.email
        const role = userData.role || 'USER'
        
        console.log('📋 Extracted user info:', { userId, username, email, role })
        
        if (!userId) {
          console.error('❌ No user ID in response')
          setErrors({ submit: 'Registration successful but user data is incomplete' })
          return
        }
        
        // Store session data
        localStorage.setItem("token", "session-" + Date.now())
        localStorage.setItem("userRole", role)
        localStorage.setItem("userId", userId.toString())
        localStorage.setItem("username", username)
        localStorage.setItem("email", email)
        localStorage.setItem("userProfile", JSON.stringify(userData))
        
        console.log('💾 Session data stored in localStorage')
        console.log('🔄 Calling onLogin with user data...')
        
        // Call onLogin to trigger routing
        onLogin(userData)
        
        console.log('✅ onLogin called successfully!')
        
      } else {
        console.error('❌ Registration failed with status:', response.status)
        const errorMessage = responseData.error || responseData.message || `Registration failed (Status: ${response.status})`
        console.error('❌ Error message:', errorMessage)
        setErrors({ submit: errorMessage })
      }
    } catch (error) {
      console.error('💥 Registration error:', error)
      setErrors({ submit: error.message || "Registration failed. Please check your connection and try again." })
    } finally {
      setLoading(false)
    }
  }

  const PasswordStrength = ({ password }) => {
    const getStrength = (pass) => {
      if (pass.length === 0) return 0
      if (pass.length < 6) return 1
      if (pass.length >= 6 && !/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(pass)) return 2
      if (/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(pass)) return 3
      return 4
    }

    const strength = getStrength(password)
    const strengthLabels = ["Very Weak", "Weak", "Fair", "Strong", "Very Strong"]
    const strengthColors = ["bg-error", "bg-orange-500", "bg-yellow-500", "bg-green-500", "bg-success"]

    return (
      <div className="mt-2">
        <div className="flex justify-between text-xs mb-1">
          <span className="text-text-muted">Password strength</span>
          <span className={`${
            strength === 0 ? 'text-text-muted' :
            strength === 1 ? 'text-error' :
            strength === 2 ? 'text-orange-500' :
            strength === 3 ? 'text-green-500' : 'text-success'
          }`}>
            {strengthLabels[strength]}
          </span>
        </div>
        <div className="w-full bg-surface-light rounded-full h-1">
          <div 
            className={`h-1 rounded-full transition-all duration-300 ${
              strengthColors[strength]
            }`}
            style={{ width: `${(strength / 4) * 100}%` }}
          ></div>
        </div>
      </div>
    )
  }

  return (
    <div className="p-8 bg-surface rounded-lg shadow-lg border border-border">
      <div className="text-center mb-2">
        <h1 className="text-3xl font-bold text-primary mb-2">CodeForge</h1>
        <p className="text-text-muted">Practice coding, master interviews</p>
      </div>

      <h2 className="text-2xl font-bold text-text mb-6 text-center">Create Account</h2>

      {errors.submit && (
        <div className="mb-4 p-3 bg-error/10 border border-error rounded-lg text-error text-sm">
          {errors.submit}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="space-y-4 mb-6">
          <div>
            <label className="block text-sm font-medium text-text mb-2">
              Username
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Choose a username"
              className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.username ? 'border-error' : 'border-border'
              } text-text placeholder-text-muted`}
            />
            {errors.username && (
              <p className="text-error text-sm mt-1">{errors.username}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-text mb-2">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter your email"
              className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.email ? 'border-error' : 'border-border'
              } text-text placeholder-text-muted`}
            />
            {errors.email && (
              <p className="text-error text-sm mt-1">{errors.email}</p>
            )}
          </div>

          <div>
            <div className="relative">
              <label className="block text-sm font-medium text-text mb-2">
                Password
              </label>
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Create a password (min 6 characters)"
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
            </div>
            {formData.password && <PasswordStrength password={formData.password} />}
            {errors.password && (
              <p className="text-error text-sm mt-1">{errors.password}</p>
            )}
          </div>

          <div className="relative">
            <label className="block text-sm font-medium text-text mb-2">
              Confirm Password
            </label>
            <input
              type={showConfirmPassword ? "text" : "password"}
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Confirm your password"
              className={`w-full px-3 py-2 bg-surface border rounded-md focus:outline-none focus:ring-2 focus:ring-primary ${
                errors.confirmPassword ? 'border-error' : 'border-border'
              } text-text placeholder-text-muted pr-10`}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-9 text-text-muted hover:text-text transition-colors"
            >
              {showConfirmPassword ? (
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
            {errors.confirmPassword && (
              <p className="text-error text-sm mt-1">{errors.confirmPassword}</p>
            )}
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
              Creating account...
            </span>
          ) : (
            "Create Account"
          )}
        </button>
      </form>

      <div className="text-center">
        <p className="text-text-muted text-sm">
          Already have an account?{" "}
          <button 
            type="button" 
            onClick={onSwitchToLogin} 
            className="text-primary hover:text-primary-light font-medium transition-colors"
          >
            Sign in
          </button>
        </p>
      </div>
    </div>
  )
}