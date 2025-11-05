import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { contestAPI, problemAPI, userAPI, adminAPI } from '../services/api';
import { Calendar, Clock, Users, Code, Plus, X, Save } from 'lucide-react';

const CreateContestForm = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [problems, setProblems] = useState([]);
  const [selectedProblems, setSelectedProblems] = useState([]);
  // Get default start time (1 hour from now) and end time (4 hours from now)
  const getDefaultTimes = () => {
    const now = new Date();
    const startTime = new Date(now.getTime() + 60 * 60 * 1000); // 1 hour from now
    const endTime = new Date(now.getTime() + 4 * 60 * 60 * 1000); // 4 hours from now
    
    // Format for datetime-local input
    const formatDateTime = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    };
    
    return {
      startTime: formatDateTime(startTime),
      endTime: formatDateTime(endTime)
    };
  };

  const defaultTimes = getDefaultTimes();

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    startTime: defaultTimes.startTime,
    endTime: defaultTimes.endTime,
    duration: 180,
    isPublic: true,
    maxParticipants: null,
    problemIds: []
  });

  useEffect(() => {
    fetchProblems();
  }, []);

  const fetchProblems = async () => {
    try {
      console.log('Fetching problems...');
      
      // Try admin API first (since contest creation is admin-only)
      let response;
      try {
        response = await adminAPI.getProblems({});
        console.log('Admin API response:', response);
      } catch (err) {
        console.warn('Admin API failed, trying problemAPI:', err);
        try {
          response = await problemAPI.getAllProblems();
        } catch (err2) {
          console.warn('problemAPI failed, trying userAPI:', err2);
          response = await userAPI.getProblems();
        }
      }
      
      console.log('Problems response:', response);
      console.log('Problems data:', response.data);
      
      if (response.data && Array.isArray(response.data)) {
        setProblems(response.data);
        console.log('Successfully loaded', response.data.length, 'problems');
      } else {
        console.warn('Invalid problems data format:', response.data);
        setProblems([]);
      }
    } catch (error) {
      console.error('Error fetching problems:', error);
      console.error('Error details:', error.response?.data);
      alert('Failed to load problems: ' + (error.response?.data?.message || error.message));
      setProblems([]);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleProblemToggle = (problemId) => {
    setSelectedProblems(prev => {
      if (prev.includes(problemId)) {
        return prev.filter(id => id !== problemId);
      } else {
        return [...prev, problemId];
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.title || !formData.startTime || !formData.endTime) {
      alert('Please fill in all required fields');
      return;
    }

    const startTime = new Date(formData.startTime);
    const endTime = new Date(formData.endTime);
    
    if (startTime >= endTime) {
      alert('End time must be after start time');
      return;
    }

    if (startTime < new Date()) {
      alert('Start time must be in the future');
      return;
    }

    try {
      setLoading(true);
      const contestData = {
        ...formData,
        problemIds: selectedProblems,
        duration: Math.floor((endTime - startTime) / 60000) // Calculate duration in minutes
      };

      const response = await contestAPI.createContest(contestData);
      alert('Contest created successfully!');
      navigate(`/contests/${response.data.id}`);
    } catch (error) {
      console.error('Error creating contest:', error);
      alert(error.response?.data?.message || 'Failed to create contest');
    } finally {
      setLoading(false);
    }
  };

  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'easy': return 'text-green-600 bg-green-100';
      case 'medium': return 'text-yellow-600 bg-yellow-100';
      case 'hard': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Create New Contest</h1>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-gray-900 flex items-center space-x-2">
              <Calendar className="w-5 h-5" />
              <span>Basic Information</span>
            </h2>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Contest Title *
              </label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., Weekly Coding Challenge #42"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Description
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows="4"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Describe the contest, rules, and what participants can expect..."
              />
            </div>
          </div>

          {/* Timing */}
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-gray-900 flex items-center space-x-2">
              <Clock className="w-5 h-5" />
              <span>Timing</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Start Time *
                </label>
                <input
                  type="datetime-local"
                  name="startTime"
                  value={formData.startTime}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  End Time *
                </label>
                <input
                  type="datetime-local"
                  name="endTime"
                  value={formData.endTime}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
          </div>

          {/* Settings */}
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-gray-900 flex items-center space-x-2">
              <Users className="w-5 h-5" />
              <span>Settings</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Max Participants (Optional)
                </label>
                <input
                  type="number"
                  name="maxParticipants"
                  value={formData.maxParticipants || ''}
                  onChange={handleChange}
                  min="1"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Leave empty for unlimited"
                />
              </div>

              <div className="flex items-center space-x-2 pt-8">
                <input
                  type="checkbox"
                  name="isPublic"
                  checked={formData.isPublic}
                  onChange={handleChange}
                  className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <label className="text-sm font-medium text-gray-700">
                  Public Contest (visible to all users)
                </label>
              </div>
            </div>
          </div>

          {/* Problem Selection */}
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-gray-900 flex items-center space-x-2">
              <Code className="w-5 h-5" />
              <span>Problems ({selectedProblems.length} selected)</span>
            </h2>

            <div className="border border-gray-300 rounded-lg max-h-96 overflow-y-auto">
              {problems.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  <Code className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                  <p>No problems available</p>
                </div>
              ) : (
                <div className="divide-y">
                  {problems.map(problem => (
                    <div
                      key={problem.id}
                      onClick={() => handleProblemToggle(problem.id)}
                      className={`p-4 cursor-pointer transition-colors ${
                        selectedProblems.includes(problem.id)
                          ? 'bg-blue-50 hover:bg-blue-100'
                          : 'hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4 flex-1">
                          <input
                            type="checkbox"
                            checked={selectedProblems.includes(problem.id)}
                            onChange={() => {}}
                            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                          />
                          <div className="flex-1">
                            <h3 className="font-semibold text-gray-900">{problem.title}</h3>
                            <div className="flex items-center space-x-3 mt-1">
                              <span className={`px-2 py-1 rounded text-xs font-semibold ${getDifficultyColor(problem.difficulty)}`}>
                                {problem.difficulty}
                              </span>
                              <span className="text-sm text-gray-600">
                                ID: {problem.id}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-between pt-6 border-t">
            <button
              type="button"
              onClick={() => navigate('/contests')}
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex items-center space-x-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Creating...</span>
                </>
              ) : (
                <>
                  <Save className="w-4 h-4" />
                  <span>Create Contest</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateContestForm;
