import React, { useState, useEffect } from 'react';
import { contestAPI } from '../services/api';
import { Trophy, Medal, Award, Crown, ArrowLeft, RefreshCw, User } from 'lucide-react';

const ContestLeaderboard = ({ contestId, onBack }) => {
  const [leaderboard, setLeaderboard] = useState([]);
  const [contest, setContest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [currentUserId, setCurrentUserId] = useState(null);

  useEffect(() => {
    fetchData();
    // Auto-refresh every 10 seconds if contest is running
    const interval = setInterval(() => {
      if (contest?.status === 'RUNNING') {
        fetchLeaderboard(true);
      }
    }, 10000);
    return () => clearInterval(interval);
  }, [contestId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        fetchContest(),
        fetchLeaderboard()
      ]);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchContest = async () => {
    try {
      const response = await contestAPI.getContest(contestId);
      setContest(response.data);
    } catch (error) {
      console.error('Error fetching contest:', error);
    }
  };

  const fetchLeaderboard = async (silent = false) => {
    try {
      if (!silent) setRefreshing(true);
      const response = await contestAPI.getContestLeaderboard(contestId);
      setLeaderboard(response.data || []);
      
      // Get current user ID from localStorage or API
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        setCurrentUserId(user.id);
      }
    } catch (error) {
      console.error('Error fetching leaderboard:', error);
    } finally {
      if (!silent) setRefreshing(false);
    }
  };

  const getRankIcon = (rank) => {
    switch (rank) {
      case 1:
        return <Crown className="w-6 h-6 text-yellow-500" />;
      case 2:
        return <Medal className="w-6 h-6 text-gray-400" />;
      case 3:
        return <Award className="w-6 h-6 text-orange-500" />;
      default:
        return null;
    }
  };

  const getRankBadgeColor = (rank) => {
    switch (rank) {
      case 1:
        return 'bg-gradient-to-r from-yellow-400 to-yellow-600 text-white';
      case 2:
        return 'bg-gradient-to-r from-gray-300 to-gray-500 text-white';
      case 3:
        return 'bg-gradient-to-r from-orange-400 to-orange-600 text-white';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const getRowStyle = (entry) => {
    if (entry.userId === currentUserId) {
      return 'bg-blue-50 border-l-4 border-blue-500';
    }
    if (entry.rank <= 3) {
      return 'bg-gradient-to-r from-yellow-50 to-transparent';
    }
    return 'hover:bg-gray-50';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          {onBack && (
            <button
              onClick={onBack}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-6 h-6 text-gray-600" />
            </button>
          )}
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Leaderboard</h1>
            <p className="text-gray-600 mt-1">{contest?.title}</p>
          </div>
        </div>
        <button
          onClick={() => fetchLeaderboard()}
          disabled={refreshing}
          className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Top 3 Podium */}
      {leaderboard.length >= 3 && (
        <div className="bg-gradient-to-br from-purple-600 to-blue-600 rounded-2xl p-8 shadow-2xl">
          <div className="flex items-end justify-center space-x-4">
            {/* 2nd Place */}
            <div className="flex flex-col items-center flex-1 max-w-xs">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-gray-300 to-gray-500 flex items-center justify-center mb-4 shadow-lg">
                <Medal className="w-12 h-12 text-white" />
              </div>
              <div className="bg-white rounded-lg p-4 w-full text-center shadow-lg">
                <div className="text-2xl font-bold text-gray-900">{leaderboard[1]?.username}</div>
                <div className="text-3xl font-bold text-gray-600 mt-2">{leaderboard[1]?.score}</div>
                <div className="text-sm text-gray-500 mt-1">{leaderboard[1]?.problemsSolved} solved</div>
              </div>
            </div>

            {/* 1st Place */}
            <div className="flex flex-col items-center flex-1 max-w-xs -mt-8">
              <div className="w-32 h-32 rounded-full bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center mb-4 shadow-2xl animate-pulse">
                <Crown className="w-16 h-16 text-white" />
              </div>
              <div className="bg-white rounded-lg p-6 w-full text-center shadow-2xl">
                <div className="text-3xl font-bold text-gray-900">{leaderboard[0]?.username}</div>
                <div className="text-4xl font-bold text-yellow-600 mt-2">{leaderboard[0]?.score}</div>
                <div className="text-sm text-gray-500 mt-1">{leaderboard[0]?.problemsSolved} solved</div>
              </div>
            </div>

            {/* 3rd Place */}
            <div className="flex flex-col items-center flex-1 max-w-xs">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-orange-400 to-orange-600 flex items-center justify-center mb-4 shadow-lg">
                <Award className="w-12 h-12 text-white" />
              </div>
              <div className="bg-white rounded-lg p-4 w-full text-center shadow-lg">
                <div className="text-2xl font-bold text-gray-900">{leaderboard[2]?.username}</div>
                <div className="text-3xl font-bold text-orange-600 mt-2">{leaderboard[2]?.score}</div>
                <div className="text-sm text-gray-500 mt-1">{leaderboard[2]?.problemsSolved} solved</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Full Leaderboard Table */}
      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b-2 border-gray-200">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Rank
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Participant
                </th>
                <th className="px-6 py-4 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Score
                </th>
                <th className="px-6 py-4 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Problems Solved
                </th>
                <th className="px-6 py-4 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Time
                </th>
                <th className="px-6 py-4 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">
                  Penalties
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {leaderboard.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                    <Trophy className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-lg font-semibold">No participants yet</p>
                    <p className="text-sm mt-2">Be the first to join and compete!</p>
                  </td>
                </tr>
              ) : (
                leaderboard.map((entry) => (
                  <tr
                    key={entry.userId}
                    className={`transition-colors ${getRowStyle(entry)}`}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center space-x-3">
                        <div className={`flex items-center justify-center w-10 h-10 rounded-full font-bold text-sm ${getRankBadgeColor(entry.rank)}`}>
                          {entry.rank <= 3 ? getRankIcon(entry.rank) : entry.rank}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center space-x-3">
                        <div className="flex-shrink-0 w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center">
                          <User className="w-5 h-5 text-white" />
                        </div>
                        <div>
                          <div className="text-sm font-semibold text-gray-900">
                            {entry.username}
                            {entry.userId === currentUserId && (
                              <span className="ml-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full">
                                You
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <div className="text-lg font-bold text-blue-600">
                        {entry.score}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <div className="text-sm font-semibold text-gray-900">
                        {entry.problemsSolved}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <div className="text-sm text-gray-600">
                        {entry.totalTime ? `${Math.floor(entry.totalTime / 60)}m` : '-'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <div className="text-sm text-gray-600">
                        {entry.penalties || 0}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stats Summary */}
      {leaderboard.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <div className="text-3xl font-bold text-blue-600">{leaderboard.length}</div>
            <div className="text-sm text-gray-600 mt-1">Total Participants</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <div className="text-3xl font-bold text-green-600">
              {Math.max(...leaderboard.map(e => e.score))}
            </div>
            <div className="text-sm text-gray-600 mt-1">Highest Score</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <div className="text-3xl font-bold text-purple-600">
              {Math.round(leaderboard.reduce((sum, e) => sum + e.score, 0) / leaderboard.length)}
            </div>
            <div className="text-sm text-gray-600 mt-1">Average Score</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <div className="text-3xl font-bold text-orange-600">
              {leaderboard.filter(e => e.problemsSolved > 0).length}
            </div>
            <div className="text-sm text-gray-600 mt-1">Active Solvers</div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestLeaderboard;
