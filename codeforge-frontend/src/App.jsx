import { useState, useEffect } from 'react'
import LoginPage from './components/LoginPage'
import AdminDashboard from './components/AdminDashboard'
import UserDashboard from './components/UserDashboard'

function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    console.log('App mounted - checking authentication...')
    // Check if user is already logged in
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    const userProfile = localStorage.getItem('userProfile')
    
    console.log('Auth check:', { token: !!token, userRole, hasProfile: !!userProfile })
    
    if (token && userRole && userProfile) {
      try {
        setUser(JSON.parse(userProfile))
        setCurrentPage(userRole === 'admin' ? 'admin-dashboard' : 'user-dashboard')
      } catch (error) {
        console.error('Error parsing user profile:', error)
        localStorage.clear()
      }
    }
    setLoading(false)
    console.log('Loading complete, currentPage:', 'login')
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
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#0f172a', color: '#f1f5f9' }}>
        <div>Loading...</div>
      </div>
    )
  }

  console.log('🎨 App render - currentPage:', currentPage, 'user:', user);

  return (
    <div className="App" style={{ minHeight: '100vh' }}>
      {console.log('🔍 Rendering condition check - currentPage:', currentPage)}
      {currentPage === 'login' && (
        <>
          {console.log('✅ Rendering LoginPage')}
          <LoginPage onLogin={handleLogin} />
        </>
      )}
      {currentPage === 'admin-dashboard' && (
        <>
          {console.log('✅ Rendering AdminDashboard')}
          <AdminDashboard onLogout={handleLogout} />
        </>
      )}
      {currentPage === 'user-dashboard' && (
        <>
          {console.log('✅ Rendering UserDashboard')}
          <UserDashboard onLogout={handleLogout} />
        </>
      )}
    </div>
  )
}

export default App