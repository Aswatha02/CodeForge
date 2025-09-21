// src/App.js
import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import Register from "./components/Register";
import Login from "./components/Login";
import ProblemList from "./components/ProblemList";
import ProblemDetail from "./components/ProblemDetail";
import CreateProblem from "./components/CreateProblem";
import CategoryList from "./components/CategoryList";
import CreateCategory from "./components/CreateCategory";
import { getUser } from "./api";

function App() {
  const user = getUser();

  return (
    <div>
      <Navbar user={user} />
      <main style={{ padding: 20 }}>
        <Routes>
          <Route path="/" element={<Navigate to="/problems" replace />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/problems" element={<ProblemList />} />
          <Route path="/problems/new" element={ user ? <CreateProblem /> : <Navigate to="/login" replace /> } />
          <Route path="/problems/:id" element={<ProblemDetail />} />
          <Route path="/categories" element={<CategoryList />} />
          <Route path="/categories/new" element={ user ? <CreateCategory /> : <Navigate to="/login" replace /> } />
        </Routes>
      </main>
    </div>
  );
}

export default App;
