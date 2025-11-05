import React, { useState, useEffect } from 'react';
import { contestAPI } from '../services/api';
import { Clock, Trophy, Users, Code, CheckCircle, XCircle, AlertCircle, ArrowLeft } from 'lucide-react';

const ContestDashboard = ({ contestId, onBack, onProblemSelect }) => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRemaining, setTimeRemaining] = useState(null);

  useEffect(() => {
    fetchDashboard();
    const interval = setInterval(fetchDashboard, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, [contestId]);

  useEffect(() => {
    if (dashboard?.contest?.timeRemaining) {
      const timer = setInterval(() => {
        setTimeRemaining(prev => prev > 0 ? prev - 1 : 0);
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [dashboard]);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const response = await contestAPI.getContestDashboard(contestId);
      console.log('📊 Contest Dashboard Response:', response.data);
      console.log('📝 Problems:', response.data.problems);
      setDashboard(response.data);
      setTimeRemaining(response.data.contest.timeRemaining);
      setError(null);
    } catch (err) {
      console.error('Error fetching dashboard:', err);
      setError(err.response?.data?.message || 'Failed to load contest dashboard');
      if (err.response?.status === 403) {
        setError('You must be registered for this contest to view the dashboard');
      }
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (seconds) => {
    if (!seconds || seconds < 0) return '00:00:00';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'easy': return 'text-green-600 bg-green-100';
      case 'medium': return 'text-yellow-600 bg-yellow-100';
      case 'hard': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-red-900 mb-2">Error</h3>
          <p className="text-red-700">{error}</p>
          <button
            onClick={onBack}
            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Back to Contests
          </button>
        </div>
      </div>
    );
  }

  const { contest, problems, userRank, topRanks, stats } = dashboard;

  const handleProblemClick = (problem) => {
    // Pass problem to parent with contest context
    if (onProblemSelect) {
      onProblemSelect(problem, contestId);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-6 space-y-6">
      {/* Back Button */}
      {onBack && (
        <button
          onClick={onBack}
          className="flex items-center space-x-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back to Contests</span>
        </button>
      )}
      
      {/* Contest Header */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg shadow-lg p-6 text-white">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold mb-2">{contest.title}</h1>
            <p className="text-blue-100">{contest.description}</p>
          </div>
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
            contest.status === 'RUNNING' ? 'bg-green-500' :
            contest.status === 'UPCOMING' ? 'bg-yellow-500' :
            'bg-gray-500'
          }`}>
            {contest.status}
          </span>
        </div>

        {/* Timer */}
        {contest.status === 'RUNNING' && (
          <div className="mt-6 bg-white/10 backdrop-blur rounded-lg p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Clock className="w-5 h-5" />
                <span className="font-semibold">Time Remaining</span>
              </div>
              <div className="text-3xl font-mono font-bold">
                {formatTime(timeRemaining)}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Your Rank</p>
              <p className="text-2xl font-bold text-blue-600">
                #{userRank?.rank || 'N/A'}
              </p>
            </div>
            <Trophy className="w-10 h-10 text-blue-600" />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Your Score</p>
              <p className="text-2xl font-bold text-green-600">
                {userRank?.score || 0}
              </p>
            </div>
            <CheckCircle className="w-10 h-10 text-green-600" />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Problems Solved</p>
              <p className="text-2xl font-bold text-purple-600">
                {userRank?.problemsSolved || 0}/{problems?.length || 0}
              </p>
            </div>
            <Code className="w-10 h-10 text-purple-600" />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Participants</p>
              <p className="text-2xl font-bold text-orange-600">
                {stats?.totalParticipants || 0}
              </p>
            </div>
            <Users className="w-10 h-10 text-orange-600" />
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Problems List */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow">
          <div className="p-6 border-b">
            <h2 className="text-xl font-bold text-gray-900">Problems</h2>
          </div>
          <div className="divide-y">
            {!problems || problems.length === 0 ? (
              <div className="p-12 text-center">
                <Code className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No Problems Yet</h3>
                <p className="text-gray-600">
                  The contest organizer hasn't added any problems to this contest yet.
                </p>
              </div>
            ) : (
              problems.map((problem, index) => (
                <div
                  key={problem.id}
                  className="p-6 hover:bg-gray-50 cursor-pointer transition-colors"
                  onClick={() => handleProblemClick(problem)}
                >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4 flex-1">
                    <div className="flex items-center justify-center w-8 h-8 rounded-full bg-gray-100 text-gray-700 font-semibold">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-3">
                        <h3 className="text-lg font-semibold text-gray-900">
                          {problem.title}
                        </h3>
                        {problem.solved && (
                          <CheckCircle className="w-5 h-5 text-green-500" />
                        )}
                      </div>
                      <div className="flex items-center space-x-4 mt-2">
                        <span className={`px-2 py-1 rounded text-xs font-semibold ${getDifficultyColor(problem.difficulty)}`}>
                          {problem.difficulty}
                        </span>
                        <span className="text-sm text-gray-600">
                          {problem.points} points
                        </span>
                        <span className="text-sm text-gray-600">
                          {problem.acceptedSubmissions}/{problem.totalSubmissions} solved
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    {problem.userAttempts > 0 && (
                      <p className="text-sm text-gray-600">
                        {problem.userAttempts} attempt{problem.userAttempts !== 1 ? 's' : ''}
                      </p>
                    )}
                  </div>
                </div>
              </div>
              ))
            )}
          </div>
        </div>

        {/* Leaderboard */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b">
            <h2 className="text-xl font-bold text-gray-900">Top 10 Leaderboard</h2>
          </div>
          <div className="divide-y max-h-[600px] overflow-y-auto">
            {topRanks?.map((entry) => (
              <div
                key={entry.userId}
                className={`p-4 ${entry.userId === userRank?.userId ? 'bg-blue-50' : ''}`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className={`flex items-center justify-center w-8 h-8 rounded-full font-bold ${
                      entry.rank === 1 ? 'bg-yellow-400 text-yellow-900' :
                      entry.rank === 2 ? 'bg-gray-300 text-gray-900' :
                      entry.rank === 3 ? 'bg-orange-400 text-orange-900' :
                      'bg-gray-100 text-gray-700'
                    }`}>
                      {entry.rank}
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900">{entry.username}</p>
                      <p className="text-sm text-gray-600">
                        {entry.problemsSolved} solved
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-lg text-blue-600">{entry.score}</p>
                    <p className="text-xs text-gray-500">points</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="p-4 border-t bg-gray-50">
            <button
              onClick={() => navigate(`/contests/${contestId}/leaderboard`)}
              className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 font-medium"
            >
              View Full Leaderboard
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContestDashboard;
