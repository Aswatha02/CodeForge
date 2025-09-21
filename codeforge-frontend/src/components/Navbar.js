// src/components/Navbar.js
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { getUser } from "../api";

export default function Navbar() {
  const navigate = useNavigate();
  const user = getUser();

  function logout() {
    localStorage.removeItem("codeforge_user");
    navigate("/login");
    window.location.reload();
  }

  return (
    <nav style={{ display: "flex", alignItems: "center", gap: 12 }}>
      <Link to="/problems">Problems</Link>
      <Link to="/categories">Categories</Link>
      <div style={{ marginLeft: "auto" }}>
        { user ? (
          <>
            <span style={{ marginRight: 10 }}>Hi, {user.username}</span>
            <button onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" style={{ marginRight: 8 }}>Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
