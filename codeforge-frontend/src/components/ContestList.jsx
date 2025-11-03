import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';

const ContestList = () => {
  const [contests, setContests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // all, upcoming, ongoing, past

  useEffect(() => {
    fetchContests();
  }, []);

  const fetchContests = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getContests();
      setContests(response.data || []);
    } catch (error) {
      console.error('Error fetching contests:', error);
    } finally {
      setLoading(false);
    }
  };

  const getContestStatus = (contest) => {
    const now = new Date();
    const startTime = new Date(contest.startTime);
    const endTime = new Date(contest.endTime);

    if (now < startTime) return 'upcoming';
    if (now >= startTime && now <= endTime) return 'ongoing';
    return 'past';
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'upcoming': return 'bg-blue-100 text-blue-800';
      case 'ongoing': return 'bg-green-100 text-green-800';
      case 'past': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  const filteredContests = contests.filter(contest => {
    if (filter === 'all') return true;
    return getContestStatus(contest) === filter;
  });

  const handleJoinContest = async (contestId) => {
    try {
      await userAPI.joinContest(contestId);
      // Refresh contests after joining
      fetchContests();
      alert('Successfully joined the contest!');
    } catch (error) {
      console.error('Error joining contest:', error);
      alert('Failed to join contest. Please try again.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">Contests</h2>

        {/* Filter Buttons */}
        <div className="flex space-x-2">
          {[
            { key: 'all', label: 'All Contests' },
            { key: 'upcoming', label: 'Upcoming' },
            { key: 'ongoing', label: 'Ongoing' },
            { key: 'past', label: 'Past' }
          ].map(option => (
            <button
              key={option.key}
              onClick={() => setFilter(option.key)}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                filter === option.key
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* Contests Grid */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      ) : filteredContests.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-gray-500 text-lg">No contests found</div>
          <div className="text-gray-400 text-sm mt-2">
            {filter === 'all' ? 'No contests available at the moment.' : `No ${filter} contests.`}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredContests.map(contest => {
            const status = getContestStatus(contest);
            return (
              <div key={contest.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
                <div className="p-6">
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-xl font-semibold text-gray-900">{contest.title}</h3>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(status)}`}>
                      {status.charAt(0).toUpperCase() + status.slice(1)}
                    </span>
                  </div>

                  <p className="text-gray-600 text-sm mb-4 line-clamp-3">
                    {contest.description}
                  </p>

                  <div className="space-y-2 mb-4">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Start:</span>
                      <span className="text-gray-900">{formatDateTime(contest.startTime)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">End:</span>
                      <span className="text-gray-900">{formatDateTime(contest.endTime)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Duration:</span>
                      <span className="text-gray-900">{contest.duration} hours</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Problems:</span>
                      <span className="text-gray-900">{contest.problemCount || 0}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Participants:</span>
                      <span className="text-gray-900">{contest.participantCount || 0}</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="text-sm text-gray-500">
                      Created by {contest.creator?.username || 'Admin'}
                    </div>
                    {status === 'upcoming' && (
                      <button
                        onClick={() => handleJoinContest(contest.id)}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                      >
                        Join Contest
                      </button>
                    )}
                    {status === 'ongoing' && (
                      <button
                        onClick={() => {/* Navigate to contest */}}
                        className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                      >
                        Enter Contest
                      </button>
                    )}
                    {status === 'past' && (
                      <button
                        onClick={() => {/* Navigate to contest results */}}
                        className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                      >
                        View Results
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Stats Summary */}
      {!loading && contests.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Contest Statistics</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">
                {contests.filter(c => getContestStatus(c) === 'upcoming').length}
              </div>
              <div className="text-sm text-gray-600">Upcoming</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {contests.filter(c => getContestStatus(c) === 'ongoing').length}
              </div>
              <div className="text-sm text-gray-600">Ongoing</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-600">
                {contests.filter(c => getContestStatus(c) === 'past').length}
              </div>
              <div className="text-sm text-gray-600">Completed</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">
                {contests.reduce((sum, c) => sum + (c.participantCount || 0), 0)}
              </div>
              <div className="text-sm text-gray-600">Total Participants</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestList;
