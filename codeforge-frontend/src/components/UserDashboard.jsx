import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import ProblemList from './ProblemList';
import CodeEditor from './CodeEditor';
import SubmissionHistory from './SubmissionHistory';
import UserProfile from './UserProfile';
import Leaderboard from './Leaderboard';
import ContestList from './ContestList';

const UserDashboard = ({ onLogout }) => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [user, setUser] = useState(null);
  const [selectedProblem, setSelectedProblem] = useState(null);
  const [stats, setStats] = useState({
    problemsSolved: 0,
    totalSubmissions: 0,
    currentStreak: 0,
    ranking: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUserData();
  }, []);

  const fetchUserData = async () => {
    try {
      setLoading(true);
      const userId = localStorage.getItem('userId');
      if (userId) {
        const response = await userAPI.getUserProfile(userId);
        setUser(response.data);
      }
      // Fetch user stats
      const statsResponse = await userAPI.getUserStats();
      setStats(statsResponse.data);
    } catch (error) {
      console.error('Error fetching user data:', error);
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    { id: 'dashboard', name: 'Dashboard', icon: '📊' },
    { id: 'problems', name: 'Problems', icon: '💡' },
    { id: 'contests', name: 'Contests', icon: '🏆' },
    { id: 'submissions', name: 'Submissions', icon: '📊' },
    { id: 'leaderboard', name: 'Leaderboard', icon: '📈' },
    { id: 'profile', name: 'Profile', icon: '👤' }
  ];

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
  );

  const QuickActionButton = ({ icon, label, onClick, color = "bg-primary" }) => (
    <button
      onClick={onClick}
      className={`${color} hover:opacity-80 text-white py-3 px-4 rounded-md transition-colors text-center flex items-center justify-center space-x-2`}
    >
      <span>{icon}</span>
      <span>{label}</span>
    </button>
  );

  const renderActiveTab = () => {
    switch (activeTab) {
      case 'dashboard':
        return (
          <div>
            <h2 className="text-3xl font-bold text-text mb-8">Welcome back, {user?.username}!</h2>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <StatCard
                title="Problems Solved"
                value={stats.problemsSolved || 0}
                color="bg-green-500/20 text-green-400"
                icon="✅"
              />
              <StatCard
                title="Total Submissions"
                value={stats.totalSubmissions || 0}
                color="bg-blue-500/20 text-blue-400"
                icon="📝"
              />
              <StatCard
                title="Current Streak"
                value={stats.currentStreak || 0}
                color="bg-orange-500/20 text-orange-400"
                icon="🔥"
              />
              <StatCard
                title="Global Ranking"
                value={`#${stats.ranking || 'N/A'}`}
                color="bg-purple-500/20 text-purple-400"
                icon="🏆"
              />
            </div>

            {/* Quick Actions */}
            <div className="bg-surface rounded-lg p-6 border border-border mb-8">
              <h3 className="text-xl font-bold text-text mb-4">Quick Actions</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <QuickActionButton
                  icon="💡"
                  label="Solve Problems"
                  onClick={() => setActiveTab('problems')}
                />
                <QuickActionButton
                  icon="🏆"
                  label="Join Contest"
                  onClick={() => setActiveTab('contests')}
                  color="bg-secondary"
                />
                <QuickActionButton
                  icon="📊"
                  label="View Submissions"
                  onClick={() => setActiveTab('submissions')}
                  color="bg-success"
                />
                <QuickActionButton
                  icon="📈"
                  label="Check Rankings"
                  onClick={() => setActiveTab('leaderboard')}
                  color="bg-warning"
                />
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-surface rounded-lg p-6 border border-border">
              <h3 className="text-xl font-bold text-text mb-4">Recent Activity</h3>
              <div className="space-y-3">
                {stats.recentActivity?.length > 0 ? (
                  stats.recentActivity.map((activity, index) => (
                    <div key={index} className="flex items-center justify-between py-2 border-b border-border last:border-b-0">
                      <div className="flex items-center space-x-4">
                        <div className="w-2 h-2 bg-primary rounded-full"></div>
                        <span className="text-text font-medium">{activity.description}</span>
                      </div>
                      <span className="text-text-muted text-sm">
                        {new Date(activity.timestamp).toLocaleDateString()}
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <div className="text-text-muted">No recent activity</div>
                    <div className="text-sm text-text-muted mt-2">Start solving problems to see your activity here!</div>
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      case 'problems':
        return <ProblemList onProblemSelect={setSelectedProblem} />;
      case 'contests':
        return <ContestList />;
      case 'submissions':
        return <SubmissionHistory />;
      case 'leaderboard':
        return <Leaderboard />;
      case 'profile':
        return <UserProfile user={user} />;
      default:
        return <ProblemList onProblemSelect={setSelectedProblem} />;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background text-text flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-text-muted">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-text">
      {/* Header */}
      <header className="bg-surface border-b border-border sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-primary">CodeForge</h1>
              <span className="ml-2 text-sm text-text-muted">User Dashboard</span>
            </div>
            <div className="flex items-center space-x-4">
              {user && (
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white text-sm font-medium">
                    {user.username?.charAt(0).toUpperCase()}
                  </div>
                  <div className="hidden md:block">
                    <p className="text-sm font-medium text-text">{user.username}</p>
                    <p className="text-xs text-text-muted">{user.email}</p>
                  </div>
                </div>
              )}
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
              {tabs.map((item) => (
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
                    {item.name}
                  </button>
                </li>
              ))}
            </ul>
          </nav>

          {/* User Stats in Sidebar */}
          {user && (
            <div className="absolute bottom-0 w-64 p-4 border-t border-border bg-surface">
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-text-muted">Problems Solved:</span>
                  <span className="text-text font-medium">{stats.problemsSolved || 0}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-text-muted">Current Streak:</span>
                  <span className="text-text font-medium">{stats.currentStreak || 0}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-text-muted">Ranking:</span>
                  <span className="text-text font-medium">#{stats.ranking || 'N/A'}</span>
                </div>
              </div>
            </div>
          )}
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          {selectedProblem ? (
            <CodeEditor
              problem={selectedProblem}
              onBack={() => setSelectedProblem(null)}
            />
          ) : (
            renderActiveTab()
          )}
        </main>
      </div>
    </div>
  );
};

export default UserDashboard;
