import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import Navbar from "./components/Navbar";
import Register from "./components/Register";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import AdminDashboard from "./components/AdminDashboard";
import ProblemList from "./components/ProblemList";
import ProblemDetail from "./components/ProblemDetail";
import CreateProblem from "./components/CreateProblem";
import CategoryList from "./components/CategoryList";
import CreateCategory from "./components/CreateCategory";
import Submissions from "./components/Submissions";
import UserProgress from "./components/UserProgress";

function AppContent() {
  const { user, isAdmin, loading } = useAuth();

  console.log('AppContent - User:', user);
  console.log('AppContent - isAdmin:', isAdmin);
  console.log('AppContent - Loading:', loading);

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#666'
      }}>
        Loading CodeForge...
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f8f9fa' }}>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/register" element={!user ? <Register /> : <Navigate to="/dashboard" replace />} />
          <Route path="/login" element={!user ? <Login /> : <Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={user ? <Dashboard /> : <Navigate to="/login" replace />} />
          <Route path="/admin" element={isAdmin ? <AdminDashboard /> : <Navigate to="/dashboard" replace />} />
          <Route path="/problems" element={<ProblemList />} />
          <Route path="/problems/new" element={isAdmin ? <CreateProblem /> : <Navigate to="/login" replace />} />
          <Route path="/problems/:id" element={<ProblemDetail />} />
          <Route path="/categories" element={<CategoryList />} />
          <Route path="/categories/:id" element={<CategoryList />} />
          <Route path="/categories/new" element={isAdmin ? <CreateCategory /> : <Navigate to="/login" replace />} />
          <Route path="/submissions" element={user ? <Submissions /> : <Navigate to="/login" replace />} />
          <Route path="/progress" element={user ? <UserProgress /> : <Navigate to="/login" replace />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;