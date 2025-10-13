import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getUserProgress, listProblems, listCategories } from "../api";

export default function Dashboard() {
  const { user } = useAuth();
  const [progress, setProgress] = useState(null);
  const [problems, setProblems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ 
    easy: 0, medium: 0, hard: 0, 
    total: 0, solved: 0 
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [progressData, problemsData, categoriesData] = await Promise.all([
          user ? getUserProgress(user.id).catch(() => null) : null,
          listProblems(),
          listCategories()
        ]);

        setProgress(progressData);
        setProblems(problemsData);
        setCategories(categoriesData);

        // Calculate stats
        const difficultyStats = problemsData.reduce((acc, problem) => {
          acc[problem.difficulty.toLowerCase()]++;
          acc.total++;
          return acc;
        }, { easy: 0, medium: 0, hard: 0, total: 0 });

        setStats({
          ...difficultyStats,
          solved: progressData?.solvedCount || 0
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user]);

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="loading">
          <div className="loading-spinner"></div>
          Loading dashboard...
        </div>
      </div>
    );
  }

  const solvedPercentage = stats.total > 0 ? Math.round((stats.solved / stats.total) * 100) : 0;

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ marginBottom: '40px' }}>
        <h1 style={{ 
          fontSize: '32px', 
          fontWeight: '700', 
          marginBottom: '8px',
          color: '#333'
        }}>
          Dashboard
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          Welcome back, {user?.username}! Continue your coding journey.
        </p>
      </div>

      {/* Stats Overview */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
        gap: '20px', 
        marginBottom: '40px' 
      }}>
        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Solved</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#28a745' }}>
            {stats.solved}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Problems</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#007bff' }}>
            {stats.total}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Problems</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Progress</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#ffc107' }}>
            {solvedPercentage}%
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Completed</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Categories</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#6f42c1' }}>
            {categories.length}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Topics</p>
        </div>
      </div>

      {/* Difficulty Breakdown */}
      <div style={{ marginBottom: '40px' }}>
        <h2 style={{ marginBottom: '20px', fontSize: '24px', fontWeight: '600' }}>
          Problem Statistics
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '16px' }}>
          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span className="difficulty-badge difficulty-easy">Easy</span>
              <span style={{ fontWeight: '600' }}>{stats.easy}</span>
            </div>
            <div style={{ 
              height: '4px', 
              background: '#e9ecef', 
              borderRadius: '2px',
              overflow: 'hidden'
            }}>
              <div style={{ 
                height: '100%', 
                background: '#28a745',
                width: `${stats.total > 0 ? (stats.easy / stats.total) * 100 : 0}%`
              }}></div>
            </div>
          </div>

          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span className="difficulty-badge difficulty-medium">Medium</span>
              <span style={{ fontWeight: '600' }}>{stats.medium}</span>
            </div>
            <div style={{ 
              height: '4px', 
              background: '#e9ecef', 
              borderRadius: '2px',
              overflow: 'hidden'
            }}>
              <div style={{ 
                height: '100%', 
                background: '#ffc107',
                width: `${stats.total > 0 ? (stats.medium / stats.total) * 100 : 0}%`
              }}></div>
            </div>
          </div>

          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span className="difficulty-badge difficulty-hard">Hard</span>
              <span style={{ fontWeight: '600' }}>{stats.hard}</span>
            </div>
            <div style={{ 
              height: '4px', 
              background: '#e9ecef', 
              borderRadius: '2px',
              overflow: 'hidden'
            }}>
              <div style={{ 
                height: '100%', 
                background: '#dc3545',
                width: `${stats.total > 0 ? (stats.hard / stats.total) * 100 : 0}%`
              }}></div>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Problems */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: '600' }}>Recommended Problems</h2>
          <Link to="/problems" className="btn btn-outline">
            View All Problems
          </Link>
        </div>

        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Problem</th>
                <th>Difficulty</th>
                <th>Categories</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {problems.slice(0, 5).map(problem => (
                <tr key={problem.id}>
                  <td>
                    <Link 
                      to={`/problems/${problem.id}`}
                      style={{ 
                        color: '#007bff', 
                        textDecoration: 'none',
                        fontWeight: '500'
                      }}
                    >
                      {problem.title}
                    </Link>
                  </td>
                  <td>
                    <span className={`difficulty-badge difficulty-${problem.difficulty.toLowerCase()}`}>
                      {problem.difficulty}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      {problem.categories?.slice(0, 2).map(cat => (
                        <span 
                          key={cat.id}
                          style={{
                            padding: '2px 8px',
                            background: '#e9ecef',
                            borderRadius: '4px',
                            fontSize: '12px',
                            color: '#666'
                          }}
                        >
                          {cat.name}
                        </span>
                      ))}
                      {problem.categories?.length > 2 && (
                        <span style={{ color: '#666', fontSize: '12px' }}>
                          +{problem.categories.length - 2} more
                        </span>
                      )}
                    </div>
                  </td>
                  <td>
                    <Link 
                      to={`/problems/${problem.id}`}
                      className="btn btn-primary"
                      style={{ padding: '6px 12px', fontSize: '12px' }}
                    >
                      Solve
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}