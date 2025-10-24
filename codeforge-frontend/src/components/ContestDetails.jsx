import { useState, useEffect } from "react"
import { contestAPI } from "../services/api"

export default function ContestDetails({ contestId, onBack }) {
  const [contest, setContest] = useState(null)
  const [leaderboard, setLeaderboard] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [activeTab, setActiveTab] = useState("overview")

  useEffect(() => {
    if (contestId) {
      fetchContestDetails()
      fetchLeaderboard()
    }
  }, [contestId])

  const fetchContestDetails = async () => {
    try {
      const response = await contestAPI.getContest(contestId)
      setContest(response.data)
    } catch (err) {
      setError("Failed to load contest details")
      console.error("Error fetching contest details:", err)
    }
  }

  const fetchLeaderboard = async () => {
    try {
      const response = await contestAPI.getContestLeaderboard(contestId)
      setLeaderboard(response.data)
    } catch (err) {
      console.error("Error fetching leaderboard:", err)
    } finally {
      setLoading(false)
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

  const getTimeRemaining = () => {
    if (!contest) return null

    const now = new Date()
    const startTime = new Date(contest.startTime)
    const endTime = new Date(contest.endTime)

    if (now < startTime) {
      const diff = startTime - now
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      return `Starts in ${hours}h ${minutes}m`
    } else if (now >= startTime && now <= endTime) {
      const diff = endTime - now
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      return `Ends in ${hours}h ${minutes}m`
    } else {
      return 'Contest ended'
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-text">Loading contest details...</div>
      </div>
    )
  }

  if (error || !contest) {
    return (
      <div className="space-y-4">
        <button
          onClick={onBack}
          className="text-primary hover:text-primary-dark flex items-center space-x-2"
        >
          ← Back to contests
        </button>
        <div className="flex justify-center items-center h-64">
          <div className="text-red-500">{error || "Contest not found"}</div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="text-primary hover:text-primary-dark flex items-center space-x-2"
        >
          ← Back to contests
        </button>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(contest.status)}`}>
          {contest.status}
        </span>
      </div>

      {/* Contest Header */}
      <div className="bg-surface rounded-lg p-6 border border-border">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-3xl font-bold text-text mb-2">{contest.title}</h1>
            <p className="text-text-muted text-lg mb-4">{contest.description}</p>
            <div className="flex items-center space-x-4 text-sm">
              <span className="text-text-muted">Created by:</span>
              <span className="text-text font-medium">{contest.createdBy?.username || 'Unknown'}</span>
              <span className="text-text-muted">•</span>
              <span className="text-text-muted">Duration:</span>
              <span className="text-text">{contest.duration} minutes</span>
            </div>
          </div>
          <div className="text-right">
            <div className="text-2xl font-bold text-primary mb-2">{getTimeRemaining()}</div>
            <div className="text-sm text-text-muted">
              {contest.maxParticipants ? `${contest.maxParticipants} max participants` : 'Unlimited participants'}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-6">
          <div>
            <span className="text-text-muted text-sm">Start Time</span>
            <div className="text-text font-medium">{formatDateTime(contest.startTime)}</div>
          </div>
          <div>
            <span className="text-text-muted text-sm">End Time</span>
            <div className="text-text font-medium">{formatDateTime(contest.endTime)}</div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="border-b border-border">
        <nav className="flex space-x-8">
          {[
            { id: "overview", label: "Overview" },
            { id: "problems", label: "Problems" },
            { id: "leaderboard", label: "Leaderboard" },
            { id: "participants", label: "Participants" }
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
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">Contest Overview</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="text-3xl font-bold text-primary mb-2">{leaderboard.length}</div>
              <div className="text-text-muted">Participants</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-success mb-2">0</div>
              <div className="text-text-muted">Problems</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-warning mb-2">{contest.duration}</div>
              <div className="text-text-muted">Minutes</div>
            </div>
          </div>
        </div>
      )}

      {activeTab === "problems" && (
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">Contest Problems</h3>
          <p className="text-text-muted">Problems will be available when the contest starts.</p>
        </div>
      )}

      {activeTab === "leaderboard" && (
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">Leaderboard</h3>
          {leaderboard.length === 0 ? (
            <p className="text-text-muted">No participants yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-2 px-4 text-text font-medium">Rank</th>
                    <th className="text-left py-2 px-4 text-text font-medium">Username</th>
                    <th className="text-left py-2 px-4 text-text font-medium">Score</th>
                    <th className="text-left py-2 px-4 text-text font-medium">Joined At</th>
                  </tr>
                </thead>
                <tbody>
                  {leaderboard.map((participant, index) => (
                    <tr key={participant.id} className="border-b border-border hover:bg-surface-light">
                      <td className="py-3 px-4 text-text font-medium">#{index + 1}</td>
                      <td className="py-3 px-4 text-text">{participant.user?.username || 'Unknown'}</td>
                      <td className="py-3 px-4 text-text">{participant.score}</td>
                      <td className="py-3 px-4 text-text-muted text-sm">
                        {new Date(participant.joinedAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === "participants" && (
        <div className="bg-surface rounded-lg p-6 border border-border">
          <h3 className="text-xl font-bold text-text mb-4">Participants</h3>
          <p className="text-text-muted">Participant list coming soon...</p>
        </div>
      )}
    </div>
  )
}
