import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { contestAPI } from '../services/api';
import { Calendar, Clock, Users, Code, Trophy, ArrowRight } from 'lucide-react';

const ContestListEnhanced = () => {
  const navigate = useNavigate();
  const [contests, setContests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [registrationStatus, setRegistrationStatus] = useState({});

  useEffect(() => {
    fetchContests();
  }, [filter]);

  const fetchContests = async () => {
    try {
      setLoading(true);
      const statusParam = filter === 'all' ? null : filter.toUpperCase();
      const response = await contestAPI.getAllContests(statusParam);
      setContests(response.data || []);
      
      // Check registration status for each contest
      const statuses = {};
      for (const contest of response.data || []) {
        try {
          const regResponse = await contestAPI.checkRegistration(contest.id);
          statuses[contest.id] = regResponse.data.isRegistered;
        } catch (err) {
          statuses[contest.id] = false;
        }
      }
      setRegistrationStatus(statuses);
    } catch (error) {
      console.error('Error fetching contests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinContest = async (contestId, e) => {
    e.stopPropagation();
    try {
      await contestAPI.joinContest(contestId);
      setRegistrationStatus(prev => ({ ...prev, [contestId]: true }));
      alert('Successfully joined the contest!');
    } catch (error) {
      console.error('Error joining contest:', error);
      alert(error.response?.data?.message || 'Failed to join contest');
    }
  };

  const handleLeaveContest = async (contestId, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to leave this contest?')) return;
    
    try {
      await contestAPI.leaveContest(contestId);
      setRegistrationStatus(prev => ({ ...prev, [contestId]: false }));
      alert('Successfully left the contest');
    } catch (error) {
      console.error('Error leaving contest:', error);
      alert(error.response?.data?.message || 'Failed to leave contest');
    }
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatTimeRemaining = (seconds) => {
    if (!seconds || seconds < 0) return 'Ended';
    
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes}m`;
  };

  const getStatusBadge = (status) => {
    const styles = {
      UPCOMING: 'bg-blue-100 text-blue-800',
      RUNNING: 'bg-green-100 text-green-800',
      COMPLETED: 'bg-gray-100 text-gray-800',
      CANCELLED: 'bg-red-100 text-red-800'
    };
    return styles[status] || styles.COMPLETED;
  };

  const handleContestClick = (contest) => {
    if (contest.status === 'RUNNING' && registrationStatus[contest.id]) {
      navigate(`/contests/${contest.id}/dashboard`);
    } else if (contest.status === 'COMPLETED') {
      navigate(`/contests/${contest.id}/leaderboard`);
    } else {
      navigate(`/contests/${contest.id}`);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Contests</h1>
          <p className="text-gray-600 mt-1">Compete with others and improve your skills</p>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="bg-white rounded-lg shadow-sm p-2 flex space-x-2">
        {[
          { key: 'all', label: 'All Contests', icon: Trophy },
          { key: 'upcoming', label: 'Upcoming', icon: Calendar },
          { key: 'running', label: 'Live', icon: Clock },
          { key: 'completed', label: 'Past', icon: Code }
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setFilter(key)}
            className={`flex-1 flex items-center justify-center space-x-2 px-4 py-3 rounded-md text-sm font-medium transition-all ${
              filter === key
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-gray-700 hover:bg-gray-100'
            }`}
          >
            <Icon className="w-4 h-4" />
            <span>{label}</span>
          </button>
        ))}
      </div>

      {/* Contests Grid */}
      {contests.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-lg shadow">
          <Trophy className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No contests found</h3>
          <p className="text-gray-600">
            {filter === 'all' ? 'No contests available at the moment' : `No ${filter} contests`}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {contests.map(contest => (
            <div
              key={contest.id}
              onClick={() => handleContestClick(contest)}
              className="bg-white rounded-lg shadow-md hover:shadow-xl transition-all cursor-pointer overflow-hidden group"
            >
              {/* Contest Header */}
              <div className={`p-4 ${
                contest.status === 'RUNNING' ? 'bg-gradient-to-r from-green-500 to-emerald-600' :
                contest.status === 'UPCOMING' ? 'bg-gradient-to-r from-blue-500 to-indigo-600' :
                'bg-gradient-to-r from-gray-500 to-gray-600'
              }`}>
                <div className="flex justify-between items-start">
                  <h3 className="text-xl font-bold text-white group-hover:scale-105 transition-transform">
                    {contest.title}
                  </h3>
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusBadge(contest.status)}`}>
                    {contest.status}
                  </span>
                </div>
                {contest.timeRemaining > 0 && (
                  <div className="mt-2 flex items-center space-x-2 text-white/90">
                    <Clock className="w-4 h-4" />
                    <span className="text-sm font-medium">
                      {contest.status === 'UPCOMING' ? 'Starts in' : 'Ends in'}: {formatTimeRemaining(contest.timeRemaining)}
                    </span>
                  </div>
                )}
              </div>

              {/* Contest Body */}
              <div className="p-6 space-y-4">
                <p className="text-gray-600 text-sm line-clamp-2 min-h-[40px]">
                  {contest.description || 'No description available'}
                </p>

                {/* Stats */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-center space-x-2 text-gray-700">
                    <Calendar className="w-4 h-4 text-gray-500" />
                    <div>
                      <p className="text-xs text-gray-500">Start</p>
                      <p className="text-sm font-semibold">{formatDateTime(contest.startTime)}</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2 text-gray-700">
                    <Clock className="w-4 h-4 text-gray-500" />
                    <div>
                      <p className="text-xs text-gray-500">Duration</p>
                      <p className="text-sm font-semibold">{contest.duration} min</p>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <div className="flex items-center space-x-4 text-sm text-gray-600">
                    <div className="flex items-center space-x-1">
                      <Users className="w-4 h-4" />
                      <span>{contest.participantCount || 0}</span>
                    </div>
                    <div className="flex items-center space-x-1">
                      <Code className="w-4 h-4" />
                      <span>{contest.problemCount || 0}</span>
                    </div>
                  </div>
                  
                  {/* Action Button */}
                  {contest.status === 'UPCOMING' && (
                    registrationStatus[contest.id] ? (
                      <button
                        onClick={(e) => handleLeaveContest(contest.id, e)}
                        className="px-4 py-2 bg-red-100 text-red-700 rounded-md text-sm font-medium hover:bg-red-200 transition-colors"
                      >
                        Leave
                      </button>
                    ) : (
                      <button
                        onClick={(e) => handleJoinContest(contest.id, e)}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 transition-colors flex items-center space-x-1"
                      >
                        <span>Join</span>
                        <ArrowRight className="w-4 h-4" />
                      </button>
                    )
                  )}
                  
                  {contest.status === 'RUNNING' && registrationStatus[contest.id] && (
                    <button
                      onClick={(e) => { e.stopPropagation(); handleContestClick(contest); }}
                      className="px-4 py-2 bg-green-600 text-white rounded-md text-sm font-medium hover:bg-green-700 transition-colors flex items-center space-x-1"
                    >
                      <span>Enter</span>
                      <ArrowRight className="w-4 h-4" />
                    </button>
                  )}
                  
                  {contest.status === 'COMPLETED' && (
                    <button
                      onClick={(e) => { e.stopPropagation(); handleContestClick(contest); }}
                      className="px-4 py-2 bg-gray-600 text-white rounded-md text-sm font-medium hover:bg-gray-700 transition-colors flex items-center space-x-1"
                    >
                      <span>Results</span>
                      <ArrowRight className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Summary Stats */}
      {contests.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Overview</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="text-3xl font-bold text-blue-600">
                {contests.filter(c => c.status === 'UPCOMING').length}
              </div>
              <div className="text-sm text-gray-600 mt-1">Upcoming</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-green-600">
                {contests.filter(c => c.status === 'RUNNING').length}
              </div>
              <div className="text-sm text-gray-600 mt-1">Live Now</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-gray-600">
                {contests.filter(c => c.status === 'COMPLETED').length}
              </div>
              <div className="text-sm text-gray-600 mt-1">Completed</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-purple-600">
                {contests.reduce((sum, c) => sum + (c.participantCount || 0), 0)}
              </div>
              <div className="text-sm text-gray-600 mt-1">Total Participants</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestListEnhanced;
