import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { logout as apiLogout } from "../api";

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  async function handleLogout() {
    setIsLoggingOut(true);
    try {
      await apiLogout();
      logout();
      navigate("/login");
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      setIsLoggingOut(false);
    }
  }

  const isActive = (path) => location.pathname === path;

  const linkStyle = (path) => ({
    padding: '8px 16px',
    borderRadius: '6px',
    textDecoration: 'none',
    color: isActive(path) ? '#007bff' : '#666',
    background: isActive(path) ? '#f0f8ff' : 'transparent',
    fontWeight: isActive(path) ? '600' : '400',
    transition: 'all 0.2s ease',
    border: 'none',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '4px'
  });

  const hoverStyle = {
    background: '#f8f9fa',
    color: '#0056b3'
  };

  return (
    <nav style={{ 
      background: 'white',
      borderBottom: '1px solid #e8e8e8',
      boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
      position: 'sticky',
      top: 0,
      zIndex: 1000
    }}>
      <div style={{ 
        maxWidth: 1200, 
        margin: '0 auto', 
        padding: '0 20px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        height: 60
      }}>
        {/* Left Section */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '32px' }}>
          <Link 
            to={isAdmin ? "/admin" : "/dashboard"}
            style={{ 
              fontSize: '24px', 
              fontWeight: 'bold', 
              color: '#007bff',
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            <span>⚡</span>
            CodeForge
          </Link>
          
          <div style={{ display: 'flex', gap: '8px' }}>
            <Link 
              to="/problems" 
              style={linkStyle('/problems')}
              onMouseEnter={(e) => {
                if (!isActive('/problems')) {
                  e.target.style.background = hoverStyle.background;
                  e.target.style.color = hoverStyle.color;
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive('/problems')) {
                  e.target.style.background = 'transparent';
                  e.target.style.color = '#666';
                }
              }}
            >
              📋 Problems
            </Link>
            
            {/* Show Dashboard only for regular users */}
            {user && !isAdmin && (
              <Link 
                to="/dashboard" 
                style={linkStyle('/dashboard')}
                onMouseEnter={(e) => {
                  if (!isActive('/dashboard')) {
                    e.target.style.background = hoverStyle.background;
                    e.target.style.color = hoverStyle.color;
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isActive('/dashboard')) {
                    e.target.style.background = 'transparent';
                    e.target.style.color = '#666';
                  }
                }}
              >
                📊 Dashboard
              </Link>
            )}
            
            {/* Show Admin link only for admins */}
            {isAdmin && (
              <Link 
                to="/admin" 
                style={linkStyle('/admin')}
                onMouseEnter={(e) => {
                  if (!isActive('/admin')) {
                    e.target.style.background = hoverStyle.background;
                    e.target.style.color = hoverStyle.color;
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isActive('/admin')) {
                    e.target.style.background = 'transparent';
                    e.target.style.color = '#666';
                  }
                }}
              >
                👑 Admin
              </Link>
            )}
          </div>
        </div>

        {/* Right Section */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {user ? (
            <>
              {/* Show Submissions and Progress only for regular users */}
              {!isAdmin && (
                <>
                  <Link 
                    to="/submissions" 
                    style={linkStyle('/submissions')}
                    onMouseEnter={(e) => {
                      if (!isActive('/submissions')) {
                        e.target.style.background = hoverStyle.background;
                        e.target.style.color = hoverStyle.color;
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isActive('/submissions')) {
                        e.target.style.background = 'transparent';
                        e.target.style.color = '#666';
                      }
                    }}
                  >
                    📄 Submissions
                  </Link>
                  
                  <Link 
                    to="/progress" 
                    style={linkStyle('/progress')}
                    onMouseEnter={(e) => {
                      if (!isActive('/progress')) {
                        e.target.style.background = hoverStyle.background;
                        e.target.style.color = hoverStyle.color;
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isActive('/progress')) {
                        e.target.style.background = 'transparent';
                        e.target.style.color = '#666';
                      }
                    }}
                  >
                    📈 Progress
                  </Link>
                </>
              )}
              
              <div style={{ 
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 12px',
                background: '#f8f9fa',
                borderRadius: '6px',
                border: '1px solid #e9ecef'
              }}>
                <span style={{ 
                  color: '#495057',
                  fontSize: '14px'
                }}>
                  👋 <strong>{user.username}</strong>
                </span>
                {isAdmin && (
                  <span style={{ 
                    background: '#007bff',
                    color: 'white',
                    padding: '2px 8px',
                    borderRadius: '12px',
                    fontSize: '12px',
                    fontWeight: '600'
                  }}>
                    ADMIN
                  </span>
                )}
              </div>
              
              <button 
                onClick={handleLogout}
                disabled={isLoggingOut}
                style={{
                  padding: '8px 16px',
                  background: 'transparent',
                  border: '1px solid #dc3545',
                  color: '#dc3545',
                  borderRadius: '6px',
                  cursor: isLoggingOut ? 'not-allowed' : 'pointer',
                  fontSize: '14px',
                  opacity: isLoggingOut ? 0.6 : 1,
                  transition: 'all 0.2s ease',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px'
                }}
                onMouseEnter={(e) => {
                  if (!isLoggingOut) {
                    e.target.style.background = '#dc3545';
                    e.target.style.color = 'white';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isLoggingOut) {
                    e.target.style.background = 'transparent';
                    e.target.style.color = '#dc3545';
                  }
                }}
              >
                {isLoggingOut ? '⏳' : '🚪'}
                {isLoggingOut ? 'Logging out...' : 'Logout'}
              </button>
            </>
          ) : (
            <div style={{ display: 'flex', gap: '12px' }}>
              <Link 
                to="/login" 
                style={{ 
                  padding: '8px 20px',
                  textDecoration: 'none',
                  color: '#007bff',
                  border: '1px solid #007bff',
                  borderRadius: '6px',
                  fontSize: '14px',
                  transition: 'all 0.2s ease',
                  background: 'transparent'
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = '#007bff';
                  e.target.style.color = 'white';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = 'transparent';
                  e.target.style.color = '#007bff';
                }}
              >
                🔑 Sign In
              </Link>
              <Link 
                to="/register" 
                style={{ 
                  padding: '8px 20px',
                  textDecoration: 'none',
                  background: '#007bff',
                  color: 'white',
                  borderRadius: '6px',
                  fontSize: '14px',
                  transition: 'all 0.2s ease',
                  border: '1px solid #007bff'
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = '#0056b3';
                  e.target.style.borderColor = '#0056b3';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = '#007bff';
                  e.target.style.borderColor = '#007bff';
                }}
              >
                ✨ Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}