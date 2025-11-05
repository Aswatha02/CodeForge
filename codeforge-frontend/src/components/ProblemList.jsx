import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import ProblemCard from './ProblemCard';

const ProblemList = ({ onProblemSelect }) => {
  const [problems, setProblems] = useState([]);
  const [filteredProblems, setFilteredProblems] = useState([]);
  const [categories, setCategories] = useState([]);
  
  // Applied filters (used for actual filtering)
  const [difficulty, setDifficulty] = useState('all');
  const [status, setStatus] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  
  // Temporary filters (used in UI before applying)
  const [tempDifficulty, setTempDifficulty] = useState('all');
  const [tempStatus, setTempStatus] = useState('all');
  const [tempSearchTerm, setTempSearchTerm] = useState('');
  const [tempSelectedCategory, setTempSelectedCategory] = useState('all');
  
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProblems();
    fetchCategories();
  }, []);

  useEffect(() => {
    filterProblems();
  }, [problems, difficulty, status, searchTerm, selectedCategory]);

  const handleApplyFilters = () => {
    setDifficulty(tempDifficulty);
    setStatus(tempStatus);
    setSearchTerm(tempSearchTerm);
    setSelectedCategory(tempSelectedCategory);
  };

  const handleResetFilters = () => {
    setTempDifficulty('all');
    setTempStatus('all');
    setTempSearchTerm('');
    setTempSelectedCategory('all');
    setDifficulty('all');
    setStatus('all');
    setSearchTerm('');
    setSelectedCategory('all');
  };

  const fetchProblems = async () => {
    try {
      const response = await userAPI.getProblems();
      setProblems(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching problems:', error);
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await userAPI.getCategories();
      setCategories(response.data);
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const filterProblems = () => {
    let filtered = [...problems]; // Start with all problems

    // Apply difficulty filter
    if (difficulty !== 'all') {
      filtered = filtered.filter(problem => problem.difficulty === difficulty);
    }

    // Apply status filter
    if (status !== 'all') {
      filtered = filtered.filter(problem => problem.userStatus === status);
    }

    // Apply category filter
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(problem =>
        problem.categories?.some(cat => cat.id === selectedCategory)
      );
    }

    // Apply search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase().trim();
      filtered = filtered.filter(problem =>
        problem.title.toLowerCase().includes(searchLower) ||
        problem.description.toLowerCase().includes(searchLower)
      );
    }

    setFilteredProblems(filtered);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background text-text flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-text-muted">Loading problems...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-text mb-2">Problems</h1>
        <p className="text-text-muted">Sharpen your coding skills with our curated collection of problems</p>
      </div>

      {/* Filters */}
      <div className="bg-surface rounded-lg border border-border p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div>
            <label className="block text-sm font-medium text-text mb-2">Search</label>
            <input
              type="text"
              placeholder="Search problems..."
              value={tempSearchTerm}
              onChange={(e) => setTempSearchTerm(e.target.value)}
              className="w-full px-3 py-2 bg-surface-light border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary text-text placeholder-text-muted"
            />
          </div>

          {/* Difficulty */}
          <div>
            <label className="block text-sm font-medium text-text mb-2">Difficulty</label>
            <select
              value={tempDifficulty}
              onChange={(e) => setTempDifficulty(e.target.value)}
              className="w-full px-3 py-2 bg-surface-light border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary text-text"
            >
              <option value="all">All Difficulties</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          {/* Category */}
          <div>
            <label className="block text-sm font-medium text-text mb-2">Category</label>
            <select
              value={tempSelectedCategory}
              onChange={(e) => setTempSelectedCategory(e.target.value)}
              className="w-full px-3 py-2 bg-surface-light border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary text-text"
            >
              <option value="all">All Categories</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>

          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-text mb-2">Status</label>
            <select
              value={tempStatus}
              onChange={(e) => setTempStatus(e.target.value)}
              className="w-full px-3 py-2 bg-surface-light border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary text-text"
            >
              <option value="all">All Status</option>
              <option value="solved">Solved</option>
              <option value="attempted">Attempted</option>
              <option value="unsolved">Unsolved</option>
            </select>
          </div>
        </div>

        {/* Filter Buttons */}
        <div className="flex gap-3 mt-4">
          <button
            onClick={handleApplyFilters}
            className="px-6 py-2 bg-primary text-white rounded-md hover:bg-primary/90 transition-colors font-medium"
          >
            Apply Filters
          </button>
          <button
            onClick={handleResetFilters}
            className="px-6 py-2 bg-surface-light text-text border border-border rounded-md hover:bg-surface-dark transition-colors font-medium"
          >
            Reset Filters
          </button>
        </div>
      </div>

      {/* Problems Table */}
      <div className="bg-surface rounded-lg border border-border overflow-hidden">
        <table className="w-full">
          <thead className="bg-surface-light border-b border-border">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Problem</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Difficulty</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Category</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Acceptance</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Submissions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filteredProblems.map(problem => (
              <tr
                key={problem.id}
                onClick={() => onProblemSelect(problem)}
                className="hover:bg-surface-light transition-colors cursor-pointer"
              >
                <td className="px-6 py-4">
                  <span className="text-lg">
                    {problem.userStatus === 'solved' ? '✅' :
                     problem.userStatus === 'attempted' ? '🟡' : '⚪'}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div>
                    <div className="text-text font-medium hover:text-primary">
                      {problem.title}
                    </div>
                    <div className="text-text-muted text-sm mt-1 line-clamp-1">
                      {problem.description}
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                    problem.difficulty === 'EASY' ? 'bg-green-500/20 text-green-400' :
                    problem.difficulty === 'MEDIUM' ? 'bg-yellow-500/20 text-yellow-400' :
                    'bg-red-500/20 text-red-400'
                  }`}>
                    {problem.difficulty}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-wrap gap-1">
                    {problem.categories?.slice(0, 2).map(category => (
                      <span
                        key={category.id}
                        className="px-2 py-1 bg-surface-light text-text-muted rounded text-xs"
                      >
                        {category.name}
                      </span>
                    ))}
                    {problem.categories?.length > 2 && (
                      <span className="text-xs text-text-muted">
                        +{problem.categories.length - 2}
                      </span>
                    )}
                  </div>
                </td>
                <td className="px-6 py-4 text-text-muted">
                  {problem.acceptanceRate || 'N/A'}%
                </td>
                <td className="px-6 py-4 text-text-muted">
                  {problem.submissionCount || 0}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {filteredProblems.length === 0 && (
          <div className="text-center py-12">
            <p className="text-text-muted text-lg">No problems found matching your criteria.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProblemList;