import { useState, useEffect } from 'react'
import LoginPage from './components/LoginPage'
import AdminDashboard from './components/AdminDashboard'
import UserDashboard from './components/UserDashboard'

function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    const userProfile = localStorage.getItem('userProfile')
    
    if (token && userRole && userProfile) {
      setUser(JSON.parse(userProfile))
      setCurrentPage(userRole === 'admin' ? 'admin-dashboard' : 'user-dashboard')
    }
    setLoading(false)
  }, [])

  const handleLogin = (userData) => {
    setUser(userData)
    setCurrentPage(userData.role === 'admin' ? 'admin-dashboard' : 'user-dashboard')
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('userProfile')
    setUser(null)
    setCurrentPage('login')
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-text">Loading...</div>
      </div>
    )
  }

  return (
    <div className="App">
      {currentPage === 'login' && <LoginPage onLogin={handleLogin} />}
      {currentPage === 'admin-dashboard' && <AdminDashboard onLogout={handleLogout} />}
      {currentPage === 'user-dashboard' && <UserDashboard onLogout={handleLogout} />}
    </div>
  )
}

export default App