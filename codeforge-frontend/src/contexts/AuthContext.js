import React, { createContext, useState, useContext, useEffect } from 'react';
import { getUser as getStoredUser, saveUser, logout as apiLogout } from '../api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = getStoredUser();
    console.log('Loaded user from storage:', storedUser);
    setUser(storedUser);
    setLoading(false);
  }, []);

  const login = (userData) => {
    console.log('Logging in user:', userData);
    saveUser(userData);
    setUser(userData);
  };

  const logout = () => {
    console.log('Logging out user');
    apiLogout();
    setUser(null);
  };

  const value = {
    user,
    login,
    logout,
    loading,
    isAuthenticated: !!user,
    isAdmin: user && user.role === 'ADMIN'
  };

  console.log('AuthContext value:', value);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};