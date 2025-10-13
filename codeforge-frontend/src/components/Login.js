import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { loginUser } from "../api";
import { useAuth } from "../contexts/AuthContext";

export default function Login() {
  const [formData, setFormData] = useState({
    usernameOrEmail: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (!formData.usernameOrEmail || !formData.password) {
        setError("Please fill in all fields");
        setLoading(false);
        return;
      }

      console.log("Logging in with:", formData);

      const user = await loginUser(formData.usernameOrEmail, formData.password);
      console.log("Login response:", user);
      
      login(user);
      
      // Redirect based on role
      setTimeout(() => {
        console.log("Login redirect, user role:", user.role);
        if (user.role === "ADMIN") {
          navigate("/admin");
        } else {
          navigate("/dashboard");
        }
      }, 100);
    } catch (err) {
      console.error("Login error:", err);
      setError(err?.data?.message || "Login failed. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      minHeight: "100vh", 
      display: "flex", 
      alignItems: "center", 
      justifyContent: "center",
      background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
    }}>
      <div style={{
        background: "white",
        padding: "40px",
        borderRadius: "12px",
        boxShadow: "0 10px 30px rgba(0,0,0,0.1)",
        width: "100%",
        maxWidth: "400px"
      }}>
        <div style={{ textAlign: "center", marginBottom: "30px" }}>
          <h1 style={{ 
            fontSize: "28px", 
            fontWeight: "700", 
            marginBottom: "8px",
            color: "#333"
          }}>
            Welcome Back
          </h1>
          <p style={{ color: "#666" }}>
            Sign in to your CodeForge account
          </p>
        </div>

        {error && (
          <div style={{
            padding: "12px 16px",
            background: "#f8d7da",
            color: "#721c24",
            border: "1px solid #f5c6cb",
            borderRadius: "6px",
            marginBottom: "20px",
            fontSize: "14px"
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: "20px" }}>
            <label style={{
              display: "block",
              marginBottom: "8px",
              fontWeight: "600",
              color: "#333",
              fontSize: "14px"
            }}>
              Username or Email *
            </label>
            <input
              type="text"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              placeholder="Enter your username or email"
              style={{
                width: "100%",
                padding: "12px",
                border: "1px solid #ddd",
                borderRadius: "6px",
                fontSize: "16px",
                boxSizing: "border-box"
              }}
              required
            />
          </div>

          <div style={{ marginBottom: "24px" }}>
            <label style={{
              display: "block",
              marginBottom: "8px",
              fontWeight: "600",
              color: "#333",
              fontSize: "14px"
            }}>
              Password *
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              style={{
                width: "100%",
                padding: "12px",
                border: "1px solid #ddd",
                borderRadius: "6px",
                fontSize: "16px",
                boxSizing: "border-box"
              }}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{
              width: "100%",
              padding: "14px",
              background: loading ? "#6c757d" : "#007bff",
              color: "white",
              border: "none",
              borderRadius: "6px",
              fontSize: "16px",
              fontWeight: "600",
              cursor: loading ? "not-allowed" : "pointer",
              marginBottom: "20px"
            }}
          >
            {loading ? "Signing In..." : "Sign In"}
          </button>

          <div style={{ textAlign: "center" }}>
            <span style={{ color: "#666" }}>
              Don't have an account?{" "}
              <Link 
                to="/register" 
                style={{ 
                  color: "#007bff", 
                  textDecoration: "none",
                  fontWeight: "500"
                }}
              >
                Sign Up
              </Link>
            </span>
          </div>
        </form>
      </div>
    </div>
  );
}