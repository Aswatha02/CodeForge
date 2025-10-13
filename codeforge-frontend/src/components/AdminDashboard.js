import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getAllUsers, getSystemStats, listProblems, listCategories, deleteProblem, deleteCategory } from "../api";
import { useAuth } from "../contexts/AuthContext";

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({ totalSubmissions: 0 });
  const [problems, setProblems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        console.log('Starting to fetch admin data...');
        
        // Fetch data with error handling for missing endpoints
        const [usersData, problemsData, categoriesData] = await Promise.all([
          getAllUsers().catch(err => {
            console.log('Users API not available, using empty array');
            return [];
          }),
          listProblems().catch(err => {
            console.log('Problems API error:', err);
            return [];
          }),
          listCategories().catch(err => {
            console.log('Categories API error:', err);
            return [];
          })
        ]);

        // Try to get stats, but don't fail if endpoint doesn't exist
        let statsData = { totalSubmissions: 0 };
        try {
          statsData = await getSystemStats();
        } catch (err) {
          console.log('Stats API not available, using default stats');
        }

        console.log('Fetched data:', {
          users: usersData,
          problems: problemsData,
          categories: categoriesData,
          stats: statsData
        });

        setUsers(usersData);
        setProblems(problemsData);
        setCategories(categoriesData);
        setStats(statsData);
      } catch (error) {
        console.error('Error fetching admin data:', error);
        setError("Failed to load admin dashboard data");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

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

  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        await deleteCategory(categoryId);
        setCategories(categories.filter(c => c.id !== categoryId));
      } catch (error) {
        alert('Failed to delete category');
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
          <p style={{ color: '#666' }}>Loading admin dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ marginBottom: '40px' }}>
        <h1 style={{ 
          fontSize: '32px', 
          fontWeight: '700', 
          marginBottom: '8px',
          color: '#333'
        }}>
          Admin Dashboard
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          Manage users, problems, and categories
        </p>
      </div>

      {error && (
        <div style={{
          padding: '12px 16px',
          background: '#f8d7da',
          color: '#721c24',
          border: '1px solid #f5c6cb',
          borderRadius: '6px',
          marginBottom: '20px'
        }}>
          {error}
        </div>
      )}

      {/* Quick Stats */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
        gap: '20px', 
        marginBottom: '40px' 
      }}>
        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total Users</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#007bff' }}>
            {users.length}
          </p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total Problems</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#28a745' }}>
            {problems.length}
          </p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Categories</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#6f42c1' }}>
            {categories.length}
          </p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total Submissions</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#ffc107' }}>
            {stats.totalSubmissions || 0}
          </p>
        </div>
      </div>

      {/* Quick Actions */}
      <div style={{ marginBottom: '40px' }}>
        <h2 style={{ marginBottom: '20px', fontSize: '24px', fontWeight: '600' }}>Quick Actions</h2>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Link to="/problems/new" className="btn btn-primary">
            Create Problem
          </Link>
          <Link to="/categories/new" className="btn btn-success">
            Create Category
          </Link>
        </div>
      </div>

      {/* Recent Users */}
      <div style={{ marginBottom: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: '600' }}>Users</h2>
          <span style={{ color: '#666', fontSize: '14px' }}>Total: {users.length}</span>
        </div>

        {users.length === 0 ? (
          <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
            <p style={{ color: '#666' }}>No users found or users API not available</p>
          </div>
        ) : (
          <div className="card">
            <table className="table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Joined</th>
                </tr>
              </thead>
              <tbody>
                {users.slice(0, 5).map(user => (
                  <tr key={user.id}>
                    <td style={{ fontWeight: '500' }}>{user.username}</td>
                    <td>{user.email}</td>
                    <td>
                      <span style={{ 
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        background: user.role === 'ADMIN' ? '#007bff' : '#6c757d',
                        color: 'white'
                      }}>
                        {user.role}
                      </span>
                    </td>
                    <td style={{ color: '#666', fontSize: '14px' }}>
                      {new Date(user.createdAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Recent Problems */}
      <div style={{ marginBottom: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: '600' }}>Problems</h2>
          <span style={{ color: '#666', fontSize: '14px' }}>Total: {problems.length}</span>
        </div>

        {problems.length === 0 ? (
          <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
            <p style={{ color: '#666' }}>No problems found</p>
            <Link to="/problems/new" className="btn btn-primary" style={{ marginTop: '16px' }}>
              Create First Problem
            </Link>
          </div>
        ) : (
          <div className="card">
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Difficulty</th>
                  <th>Categories</th>
                  <th>Actions</th>
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
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        background: 
                          problem.difficulty === 'EASY' ? '#28a745' :
                          problem.difficulty === 'MEDIUM' ? '#ffc107' : '#dc3545',
                        color: 'white',
                        textTransform: 'capitalize'
                      }}>
                        {problem.difficulty?.toLowerCase()}
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
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <Link 
                          to={`/problems/${problem.id}`}
                          className="btn btn-primary"
                          style={{ padding: '4px 8px', fontSize: '12px' }}
                        >
                          View
                        </Link>
                        <button
                          onClick={() => handleDeleteProblem(problem.id)}
                          className="btn btn-danger"
                          style={{ padding: '4px 8px', fontSize: '12px' }}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Categories */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: '600' }}>Categories</h2>
          <span style={{ color: '#666', fontSize: '14px' }}>Total: {categories.length}</span>
        </div>

        {categories.length === 0 ? (
          <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
            <p style={{ color: '#666' }}>No categories found</p>
            <Link to="/categories/new" className="btn btn-success" style={{ marginTop: '16px' }}>
              Create First Category
            </Link>
          </div>
        ) : (
          <div className="card">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Description</th>
                  <th>Problem Count</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {categories.slice(0, 5).map(category => (
                  <tr key={category.id}>
                    <td style={{ fontWeight: '500' }}>{category.name}</td>
                    <td style={{ color: '#666' }}>{category.description}</td>
                    <td>{category.problems?.length || 0}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <Link 
                          to={`/categories/${category.id}`}
                          className="btn btn-primary"
                          style={{ padding: '4px 8px', fontSize: '12px' }}
                        >
                          View
                        </Link>
                        <button
                          onClick={() => handleDeleteCategory(category.id)}
                          className="btn btn-danger"
                          style={{ padding: '4px 8px', fontSize: '12px' }}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
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