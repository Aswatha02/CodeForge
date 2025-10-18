import { useState, useEffect } from "react"

export default function UserDashboard({ onLogout }) {
  const [user, setUser] = useState(null)
  const [activeTab, setActiveTab] = useState("overview")
  const [userStats, setUserStats] = useState({
    problemsSolved: 0,
    totalSubmissions: 0,
    accuracy: 0,
    rank: 0,
    easy: 0,
    medium: 0,
    hard: 0
  })

  useEffect(() => {
    // Get user data from localStorage
    const userProfile = localStorage.getItem("userProfile")
    if (userProfile) {
      setUser(JSON.parse(userProfile))
    }

    // Mock user stats (replace with API call)
    setUserStats({
      problemsSolved: 24,
      totalSubmissions: 156,
      accuracy: 65.4,
      rank: 1247,
      easy: 15,
      medium: 8,
      hard: 1
    })
  }, [])

  if (!user) {
    return <div>Loading...</div>
  }

  const StatCard = ({ title, value, subtitle, color = "text-text" }) => (
    <div className="bg-surface rounded-lg p-6 border border-border">
      <p className="text-text-muted text-sm mb-2">{title}</p>
      <p className={`text-3xl font-bold ${color} mb-1`}>{value}</p>
      {subtitle && <p className="text-text-muted text-sm">{subtitle}</p>}
    </div>
  )

  const ProgressBar = ({ label, solved, total, color }) => (
    <div className="mb-4">
      <div className="flex justify-between text-sm mb-1">
        <span className="text-text">{label}</span>
        <span className="text-text-muted">{solved}/{total}</span>
      </div>
      <div className="w-full bg-surface-light rounded-full h-2">
        <div 
          className={`h-2 rounded-full ${color}`}
          style={{ width: `${(solved / total) * 100}%` }}
        ></div>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-background text-text">
      {/* Header */}
      <header className="bg-surface border-b border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-primary">CodeForge</h1>
              <nav className="ml-10 flex space-x-8">
                {["Problems", "Contests", "Discuss", "Learn"].map((item) => (
                  <button key={item} className="text-text hover:text-primary transition-colors">
                    {item}
                  </button>
                ))}
              </nav>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-text">Welcome, {user.username}</span>
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

      <div className="max-w-7xl mx-auto p-8">
        {/* User Profile Header */}
        <div className="bg-surface rounded-lg p-6 border border-border mb-8">
          <div className="flex items-center space-x-6">
            <div className="w-20 h-20 bg-primary rounded-full flex items-center justify-center text-white text-2xl font-bold">
              {user.username.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="text-2xl font-bold text-text">{user.username}</h2>
              <p className="text-text-muted">{user.email}</p>
              <div className="flex space-x-4 mt-2">
                <span className="bg-primary/20 text-primary px-3 py-1 rounded-full text-sm">
                  Rank: #{userStats.rank}
                </span>
                <span className="bg-success/20 text-success px-3 py-1 rounded-full text-sm">
                  {userStats.problemsSolved} Problems Solved
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="border-b border-border mb-8">
          <nav className="flex space-x-8">
            {[
              { id: "overview", label: "Overview" },
              { id: "problems", label: "Problems" },
              { id: "submissions", label: "Submissions" },
              { id: "contests", label: "Contests" },
              { id: "profile", label: "Profile" }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? "border-primary text-primary"
                    : "border-transparent text-text-muted hover:text-text"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        {activeTab === "overview" && (
          <div className="space-y-8">
            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <StatCard title="Problems Solved" value={userStats.problemsSolved} />
              <StatCard title="Total Submissions" value={userStats.totalSubmissions} />
              <StatCard title="Accuracy" value={`${userStats.accuracy}%`} color="text-success" />
              <StatCard title="Global Rank" value={`#${userStats.rank}`} />
            </div>

            {/* Progress by Difficulty */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="bg-surface rounded-lg p-6 border border-border">
                <h3 className="text-xl font-bold text-text mb-4">Progress by Difficulty</h3>
                <ProgressBar 
                  label="Easy" 
                  solved={userStats.easy} 
                  total={50} 
                  color="bg-success" 
                />
                <ProgressBar 
                  label="Medium" 
                  solved={userStats.medium} 
                  total={100} 
                  color="bg-warning" 
                />
                <ProgressBar 
                  label="Hard" 
                  solved={userStats.hard} 
                  total={50} 
                  color="bg-error" 
                />
              </div>

              {/* Recent Activity */}
              <div className="bg-surface rounded-lg p-6 border border-border">
                <h3 className="text-xl font-bold text-text mb-4">Recent Activity</h3>
                <div className="space-y-3">
                  {[
                    { action: "Solved", problem: "Two Sum", difficulty: "Easy", time: "2 hours ago" },
                    { action: "Attempted", problem: "Reverse Linked List", difficulty: "Medium", time: "1 day ago" },
                    { action: "Solved", problem: "Valid Parentheses", difficulty: "Easy", time: "2 days ago" },
                    { action: "Joined", problem: "Weekly Contest", difficulty: "", time: "3 days ago" }
                  ].map((activity, index) => (
                    <div key={index} className="flex items-center justify-between py-2 border-b border-border last:border-b-0">
                      <div className="flex items-center space-x-3">
                        <span className={`text-xs px-2 py-1 rounded ${
                          activity.difficulty === 'Easy' ? 'bg-success/20 text-success' :
                          activity.difficulty === 'Medium' ? 'bg-warning/20 text-warning' :
                          activity.difficulty === 'Hard' ? 'bg-error/20 text-error' : 'bg-primary/20 text-primary'
                        }`}>
                          {activity.action}
                        </span>
                        <span className="text-text font-medium">{activity.problem}</span>
                        {activity.difficulty && (
                          <span className="text-text-muted text-sm">{activity.difficulty}</span>
                        )}
                      </div>
                      <span className="text-text-muted text-sm">{activity.time}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-surface rounded-lg p-6 border border-border">
              <h3 className="text-xl font-bold text-text mb-4">Quick Actions</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <button className="bg-primary hover:bg-primary-dark text-white py-3 px-4 rounded-md transition-colors text-center">
                  Practice Problems
                </button>
                <button className="bg-secondary hover:bg-secondary-dark text-white py-3 px-4 rounded-md transition-colors text-center">
                  Join Contest
                </button>
                <button className="bg-success hover:bg-success/80 text-white py-3 px-4 rounded-md transition-colors text-center">
                  View Leaderboard
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === "problems" && (
          <div className="bg-surface rounded-lg p-6 border border-border">
            <h3 className="text-xl font-bold text-text mb-4">Problem Sets</h3>
            <p className="text-text-muted">Problem sets and practice interface coming soon...</p>
          </div>
        )}

        {activeTab === "submissions" && (
          <div className="bg-surface rounded-lg p-6 border border-border">
            <h3 className="text-xl font-bold text-text mb-4">Submission History</h3>
            <p className="text-text-muted">Submission history and code review coming soon...</p>
          </div>
        )}

        {activeTab === "contests" && (
          <div className="bg-surface rounded-lg p-6 border border-border">
            <h3 className="text-xl font-bold text-text mb-4">Contests</h3>
            <p className="text-text-muted">Contest participation and results coming soon...</p>
          </div>
        )}

        {activeTab === "profile" && (
          <div className="bg-surface rounded-lg p-6 border border-border">
            <h3 className="text-xl font-bold text-text mb-4">Profile Settings</h3>
            <p className="text-text-muted">Profile customization and settings coming soon...</p>
          </div>
        )}
      </div>
    </div>
  )
}