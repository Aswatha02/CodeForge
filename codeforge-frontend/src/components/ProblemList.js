import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { listProblems, listCategories, getProblemsByCategory, deleteProblem } from "../api";

export default function ProblemList() {
  const { id } = useParams();
  const [problems, setProblems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(id || 'all');
  const [difficultyFilter, setDifficultyFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user, isAdmin } = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [problemsData, categoriesData] = await Promise.all([
          selectedCategory === 'all' ? listProblems() : getProblemsByCategory(selectedCategory),
          listCategories()
        ]);
        setProblems(problemsData);
        setCategories(categoriesData);
      } catch (err) {
        setError(err?.data?.message || 'Failed to fetch problems');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [selectedCategory]);

  const filteredProblems = problems.filter(problem => {
    if (difficultyFilter === 'all') return true;
    return problem.difficulty === difficultyFilter.toUpperCase();
  });

  const getProblemsCountByDifficulty = () => {
    const counts = { all: problems.length, easy: 0, medium: 0, hard: 0 };
    problems.forEach(problem => {
      const difficulty = problem.difficulty?.toLowerCase();
      if (counts.hasOwnProperty(difficulty)) {
        counts[difficulty]++;
      }
    });
    return counts;
  };

  const difficultyCounts = getProblemsCountByDifficulty();

  const handleDeleteProblem = async (problemId) => {
    if (window.confirm('Are you sure you want to delete this problem?')) {
      try {
        await deleteProblem(problemId);
        setProblems(problems.filter(p => p.id !== problemId));
      } catch (error) {
        alert('Failed to delete problem');
      }
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center',
          flexDirection: 'column',
          gap: '16px'
        }}>
          <div style={{
            width: '40px',
            height: '40px',
            border: '4px solid #f3f3f3',
            borderTop: '4px solid #007bff',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          <p style={{ color: '#666' }}>Loading problems...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ 
          fontSize: '32px', 
          fontWeight: '700', 
          marginBottom: '8px',
          color: '#333'
        }}>
          Problems
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          Practice coding problems and improve your skills
        </p>
      </div>

      {error && (
        <div style={{
          padding: '12px 16px',
          background: '#f8d7da',
          color: '#721c24',
          border: '1px solid #f5c6cb',
          borderRadius: '6px',
          marginBottom: '24px'
        }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '250px 1fr', gap: '32px' }}>
        {/* Filters Sidebar */}
        <div>
          <div style={{ 
            background: 'white',
            padding: '20px', 
            borderRadius: '8px',
            border: '1px solid #e8e8e8',
            position: 'sticky', 
            top: '80px' 
          }}>
            <h3 style={{ marginBottom: '16px', fontSize: '18px', fontWeight: '600' }}>Categories</h3>
            <div style={{ marginBottom: '24px' }}>
              <button
                onClick={() => setSelectedCategory('all')}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  textAlign: 'left',
                  background: selectedCategory === 'all' ? '#f0f8ff' : 'transparent',
                  border: 'none',
                  borderRadius: '6px',
                  color: selectedCategory === 'all' ? '#007bff' : '#666',
                  fontWeight: selectedCategory === 'all' ? '600' : '400',
                  cursor: 'pointer',
                  marginBottom: '4px',
                  transition: 'all 0.2s ease'
                }}
              >
                All Categories ({problems.length})
              </button>
              {categories.map(category => (
                <button
                  key={category.id || category._id}
                  onClick={() => setSelectedCategory(category.id || category._id)}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    textAlign: 'left',
                    background: selectedCategory === (category.id || category._id) ? '#f0f8ff' : 'transparent',
                    border: 'none',
                    borderRadius: '6px',
                    color: selectedCategory === (category.id || category._id) ? '#007bff' : '#666',
                    fontWeight: selectedCategory === (category.id || category._id) ? '600' : '400',
                    cursor: 'pointer',
                    marginBottom: '4px',
                    transition: 'all 0.2s ease'
                  }}
                >
                  {category.name} ({category.problems?.length || 0})
                </button>
              ))}
            </div>

            <h3 style={{ marginBottom: '16px', fontSize: '18px', fontWeight: '600' }}>Difficulty</h3>
            <div>
              {['all', 'easy', 'medium', 'hard'].map(diff => (
                <button
                  key={diff}
                  onClick={() => setDifficultyFilter(diff)}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    textAlign: 'left',
                    background: difficultyFilter === diff ? '#f0f8ff' : 'transparent',
                    border: 'none',
                    borderRadius: '6px',
                    color: difficultyFilter === diff ? '#007bff' : '#666',
                    fontWeight: difficultyFilter === diff ? '600' : '400',
                    cursor: 'pointer',
                    marginBottom: '4px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'all 0.2s ease',
                    textTransform: 'capitalize'
                  }}
                >
                  <span>{diff}</span>
                  <span style={{ 
                    fontSize: '12px', 
                    color: difficultyFilter === diff ? '#007bff' : '#999',
                    background: difficultyFilter === diff ? 'rgba(0,123,255,0.1)' : '#f8f9fa',
                    padding: '2px 6px',
                    borderRadius: '8px'
                  }}>
                    {difficultyCounts[diff]}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Problems List */}
        <div>
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            marginBottom: '24px'
          }}>
            <h2 style={{ fontSize: '24px', fontWeight: '600' }}>
              {selectedCategory === 'all' ? 'All Problems' : 
               categories.find(c => (c.id || c._id) == selectedCategory)?.name + ' Problems'}
              <span style={{ 
                marginLeft: '12px',
                fontSize: '16px',
                color: '#666',
                fontWeight: '400'
              }}>
                ({filteredProblems.length})
              </span>
            </h2>
            
            {isAdmin && (
              <Link 
                to="/problems/new" 
                style={{
                  padding: '10px 20px',
                  background: '#007bff',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '6px',
                  fontWeight: '500',
                  fontSize: '14px',
                  border: 'none',
                  cursor: 'pointer',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px'
                }}
              >
                <span>+</span>
                Create Problem
              </Link>
            )}
          </div>

          {filteredProblems.length === 0 ? (
            <div style={{ 
              background: 'white',
              padding: '60px 40px', 
              borderRadius: '8px',
              border: '1px solid #e8e8e8',
              textAlign: 'center' 
            }}>
              <div style={{ fontSize: '48px', marginBottom: '20px' }}>📝</div>
              <h3 style={{ marginBottom: '12px', color: '#666' }}>No problems found</h3>
              <p style={{ color: '#999', marginBottom: '20px' }}>
                {selectedCategory !== 'all' || difficultyFilter !== 'all' 
                  ? 'Try changing your filters' 
                  : 'No problems available yet'}
              </p>
              {isAdmin && problems.length === 0 && (
                <Link 
                  to="/problems/new" 
                  style={{
                    padding: '12px 24px',
                    background: '#007bff',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '6px',
                    fontWeight: '500'
                  }}
                >
                  Create First Problem
                </Link>
              )}
            </div>
          ) : (
            <div style={{ 
              background: 'white',
              borderRadius: '8px',
              border: '1px solid #e8e8e8',
              overflow: 'hidden'
            }}>
              <div style={{
                display: 'grid',
                gridTemplateColumns: '60px 1fr 120px 150px 100px',
                gap: '16px',
                padding: '16px 20px',
                background: '#f8f9fa',
                borderBottom: '1px solid #e8e8e8',
                fontWeight: '600',
                color: '#333'
              }}>
                <div>#</div>
                <div>Problem</div>
                <div>Difficulty</div>
                <div>Category</div>
                <div>Action</div>
              </div>
              
              {filteredProblems.map((problem, index) => (
                <div
                  key={problem.id || problem._id}
                  style={{
                    display: 'grid',
                    gridTemplateColumns: '60px 1fr 120px 150px 100px',
                    gap: '16px',
                    padding: '20px',
                    borderBottom: '1px solid #f0f0f0',
                    alignItems: 'center'
                  }}
                >
                  <div style={{ color: '#666', fontWeight: '500' }}>
                    {index + 1}
                  </div>
                  <div>
                    <Link 
                      to={`/problems/${problem.id || problem._id}`}
                      style={{ 
                        color: '#007bff', 
                        textDecoration: 'none',
                        fontWeight: '500',
                        fontSize: '16px',
                        display: 'block',
                        marginBottom: '4px'
                      }}
                    >
                      {problem.title}
                    </Link>
                    <div style={{ 
                      color: '#666', 
                      fontSize: '14px'
                    }}>
                      {problem.description?.substring(0, 100)}
                      {problem.description?.length > 100 ? '...' : ''}
                    </div>
                  </div>
                  <div>
                    <span style={{
                      padding: '4px 12px',
                      background: 
                        problem.difficulty === 'EASY' ? '#d4edda' :
                        problem.difficulty === 'MEDIUM' ? '#fff3cd' : '#f8d7da',
                      color: 
                        problem.difficulty === 'EASY' ? '#155724' :
                        problem.difficulty === 'MEDIUM' ? '#856404' : '#721c24',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: '500',
                      textTransform: 'capitalize'
                    }}>
                      {problem.difficulty?.toLowerCase()}
                    </span>
                  </div>
                  <div>
                    <span style={{
                      padding: '4px 8px',
                      background: '#e9ecef',
                      borderRadius: '4px',
                      fontSize: '12px',
                      color: '#666'
                    }}>
                      {problem.category?.name || 'Uncategorized'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <Link
                      to={`/problems/${problem.id || problem._id}`}
                      style={{
                        padding: '6px 12px',
                        background: '#007bff',
                        color: 'white',
                        textDecoration: 'none',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: '500'
                      }}
                    >
                      Solve
                    </Link>
                    {isAdmin && (
                      <button
                        onClick={() => handleDeleteProblem(problem.id || problem._id)}
                        style={{
                          padding: '6px 12px',
                          background: '#dc3545',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          fontSize: '12px',
                          fontWeight: '500',
                          cursor: 'pointer'
                        }}
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
}