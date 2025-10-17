import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Load user from localStorage on app start
  useEffect(() => {
    const loadUser = () => {
      try {
        const storedUser = localStorage.getItem('codeforge_user');
        if (storedUser) {
          const userData = JSON.parse(storedUser);
          setUser(userData);
          setIsAuthenticated(true);
          console.log('Loaded user from storage:', userData);
        }
      } catch (error) {
        console.error('Error loading user from storage:', error);
        localStorage.removeItem('codeforge_user');
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, []);

  const login = (userData) => {
    console.log('AuthContext login called with:', userData);
    
    if (userData && userData.token) {
      setUser(userData);
      setIsAuthenticated(true);
      localStorage.setItem('codeforge_user', JSON.stringify(userData));
      console.log('User saved to storage');
    } else {
      console.error('Invalid user data in login:', userData);
    }
  };

  const logout = () => {
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem('codeforge_user');
    console.log('User logged out');
  };

  // Check if user is admin
  const isAdmin = () => {
    return user?.user?.role === 'ADMIN';
  };

  const value = {
    user,
    loading,
    isAuthenticated,
    isAdmin: isAdmin(), // Compute this value
    login,
    logout
  };

  console.log('AuthContext value:', value);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}