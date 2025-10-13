import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { listCategories, getCategory, getProblemsByCategory } from "../api";

export default function CategoryList() {
  const { id } = useParams();
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        if (id) {
          // View specific category
          const [categoryData, problemsData] = await Promise.all([
            getCategory(id),
            getProblemsByCategory(id)
          ]);
          setSelectedCategory(categoryData);
          setProblems(problemsData);
          setCategories([]); // Don't need all categories when viewing one
        } else {
          // View all categories
          const categoriesData = await listCategories();
          setCategories(categoriesData);
          setSelectedCategory(null);
        }
      } catch (error) {
        console.error('Error fetching category data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="loading">
          <div className="loading-spinner"></div>
          Loading categories...
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      {selectedCategory ? (
        // Single Category View
        <div>
          <div style={{ marginBottom: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <h1 style={{ 
                  fontSize: '32px', 
                  fontWeight: '700', 
                  marginBottom: '8px',
                  color: '#333'
                }}>
                  {selectedCategory.name}
                </h1>
                <p style={{ color: '#666', fontSize: '16px', maxWidth: '600px' }}>
                  {selectedCategory.description}
                </p>
              </div>
              <Link to="/categories" className="btn btn-outline">
                Back to Categories
              </Link>
            </div>
          </div>

          {/* Problems in this category */}
          <div>
            <h2 style={{ marginBottom: '20px', fontSize: '24px', fontWeight: '600' }}>
              Problems ({problems.length})
            </h2>
            
            {problems.length === 0 ? (
              <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
                <h3 style={{ marginBottom: '12px', color: '#666' }}>No problems yet</h3>
                <p style={{ color: '#999', marginBottom: '20px' }}>
                  There are no problems in this category yet.
                </p>
                <Link to="/problems/new" className="btn btn-primary">
                  Create Problem
                </Link>
              </div>
            ) : (
              <div className="card">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Problem</th>
                      <th style={{ width: '120px' }}>Difficulty</th>
                      <th style={{ width: '100px' }}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {problems.map(problem => (
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
                          <div style={{ 
                            color: '#666', 
                            fontSize: '14px',
                            marginTop: '4px'
                          }}>
                            {problem.description?.substring(0, 100)}
                            {problem.description?.length > 100 ? '...' : ''}
                          </div>
                        </td>
                        <td>
                          <span className={`difficulty-badge difficulty-${problem.difficulty.toLowerCase()}`}>
                            {problem.difficulty}
                          </span>
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
            )}
          </div>
        </div>
      ) : (
        // All Categories View
        <div>
          <div style={{ marginBottom: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
              <div>
                <h1 style={{ 
                  fontSize: '32px', 
                  fontWeight: '700', 
                  marginBottom: '8px',
                  color: '#333'
                }}>
                  Categories
                </h1>
                <p style={{ color: '#666', fontSize: '16px' }}>
                  Browse problems by category and topic
                </p>
              </div>
              <Link to="/categories/new" className="btn btn-primary">
                Create Category
              </Link>
            </div>
          </div>

          {categories.length === 0 ? (
            <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
              <h3 style={{ marginBottom: '12px', color: '#666' }}>No categories yet</h3>
              <p style={{ color: '#999', marginBottom: '20px' }}>
                Get started by creating your first category.
              </p>
              <Link to="/categories/new" className="btn btn-primary">
                Create Category
              </Link>
            </div>
          ) : (
            <div style={{ 
              display: 'grid', 
              gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', 
              gap: '24px' 
            }}>
              {categories.map(category => (
                <div key={category.id} className="card" style={{ padding: '24px' }}>
                  <div style={{ marginBottom: '16px' }}>
                    <h3 style={{ 
                      fontSize: '20px', 
                      fontWeight: '600', 
                      marginBottom: '8px',
                      color: '#333'
                    }}>
                      {category.name}
                    </h3>
                    <p style={{ 
                      color: '#666', 
                      fontSize: '14px',
                      lineHeight: '1.5'
                    }}>
                      {category.description}
                    </p>
                  </div>
                  
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    marginBottom: '16px'
                  }}>
                    <span style={{ color: '#999', fontSize: '14px' }}>
                      {category.problems?.length || 0} problems
                    </span>
                  </div>

                  <Link 
                    to={`/categories/${category.id}`}
                    className="btn btn-primary"
                    style={{ width: '100%', textAlign: 'center' }}
                  >
                    View Problems
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}