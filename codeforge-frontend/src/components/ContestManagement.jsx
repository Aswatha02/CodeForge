import { useState, useEffect } from "react"
import { contestAPI } from "../services/api"

export default function ContestManagement() {
  const [activeTab, setActiveTab] = useState("browse")
  const [contests, setContests] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchContests()
  }, [])

  const fetchContests = async () => {
    try {
      setLoading(true)
      const response = await contestAPI.getAllContests()
      setContests(response.data)
    } catch (err) {
      setError("Failed to load contests")
      console.error("Error fetching contests:", err)
    } finally {
      setLoading(false)
    }
  }

  const handleJoinContest = async (contestId) => {
    try {
      await contestAPI.joinContest(contestId)
      // Refresh contests to update join status
      await fetchContests()
      alert("Successfully joined contest!")
    } catch (err) {
      alert("Failed to join contest: " + (err.response?.data?.message || err.message))
    }
  }

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString()
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'UPCOMING': return 'bg-blue-100 text-blue-800'
      case 'RUNNING': return 'bg-green-100 text-green-800'
      case 'COMPLETED': return 'bg-gray-100 text-gray-800'
      case 'CANCELLED': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const ContestCard = ({ contest }) => (
    <div className="bg-surface rounded-lg p-6 border border-border hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold text-text mb-2">{contest.title}</h3>
          <p className="text-text-muted text-sm mb-2">{contest.description}</p>
        </div>
        <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(contest.status)}`}>
          {contest.status}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
        <div>
          <span className="text-text-muted">Start:</span>
          <span className="text-text ml-2">{formatDateTime(contest.startTime)}</span>
        </div>
        <div>
          <span className="text-text-muted">End:</span>
          <span className="text-text ml-2">{formatDateTime(contest.endTime)}</span>
        </div>
        <div>
          <span className="text-text-muted">Duration:</span>
          <span className="text-text ml-2">{contest.duration} minutes</span>
        </div>
        <div>
          <span className="text-text-muted">Max Participants:</span>
          <span className="text-text ml-2">{contest.maxParticipants || 'Unlimited'}</span>
        </div>
      </div>

      <div className="flex justify-between items-center">
        <div className="text-sm text-text-muted">
          Created by: {contest.createdBy?.username || 'Unknown'}
        </div>
        <button
          onClick={() => handleJoinContest(contest.id)}
          disabled={contest.status !== 'UPCOMING'}
          className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
            contest.status === 'UPCOMING'
              ? 'bg-primary hover:bg-primary-dark text-white'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          {contest.status === 'UPCOMING' ? 'Join Contest' : contest.status.toLowerCase()}
        </button>
      </div>
    </div>
  )

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-text">Loading contests...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-red-500">{error}</div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold text-text">Contest Management</h2>
        <button
          onClick={fetchContests}
          className="bg-primary hover:bg-primary-dark text-white px-4 py-2 rounded-md transition-colors"
        >
          Refresh
        </button>
      </div>

      {/* Navigation Tabs */}
      <div className="border-b border-border">
        <nav className="flex space-x-8">
          {[
            { id: "browse", label: "Browse Contests" },
            { id: "my-contests", label: "My Contests" },
            { id: "active", label: "Active Contest" }
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
      {activeTab === "browse" && (
        <div className="space-y-6">
          <div className="flex justify-between items-center">
            <h3 className="text-xl font-bold text-text">Available Contests</h3>
            <div className="flex space-x-2">
              <button className="px-3 py-1 text-sm bg-surface border border-border rounded-md hover:bg-surface-light">
                All
              </button>
              <button className="px-3 py-1 text-sm bg-surface border border-border rounded-md hover:bg-surface-light">
                Upcoming
              </button>
              <button className="px-3 py-1 text-sm bg-surface border border-border rounded-md hover:bg-surface-light">
                Running
              </button>
              <button className="px-3 py-1 text-sm bg-surface border border-border rounded-md hover:bg-surface-light">
                Completed
              </button>
            </div>
          </div>

          {contests.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-text-muted">No contests available at the moment.</p>
            </div>
          ) : (
            <div className="grid gap-6">
              {contests.map((contest) => (
                <ContestCard key={contest.id} contest={contest} />
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === "my-contests" && (
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">My Contests</h3>
          <p className="text-text-muted">Contest participation history coming soon...</p>
        </div>
      )}

      {activeTab === "active" && (
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">Active Contest</h3>
          <p className="text-text-muted">Active contest participation interface coming soon...</p>
        </div>
      )}
    </div>
  )
}
