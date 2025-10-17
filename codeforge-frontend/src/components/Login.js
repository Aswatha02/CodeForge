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

      console.log("🔐 Attempting login with:", formData);

      const response = await loginUser(formData.usernameOrEmail, formData.password);
      console.log("✅ Login response:", response);
      
      // Detailed response analysis
      console.log("📊 Response analysis:");
      console.log("- Has response?", !!response);
      console.log("- Response keys:", response ? Object.keys(response) : 'No response');
      console.log("- Has token?", !!response?.token);
      console.log("- Has user?", !!response?.user);
      console.log("- User role:", response?.user?.role);
      
      if (!response) {
        throw new Error("No response from server - endpoint might not exist");
      }

      let userData;
      let userRole;

      if (response.token && response.user) {
        // New JWT format
        userData = response;
        userRole = response.user.role;
        console.log("🔄 Using JWT format");
      } else if (response.id) {
        // Old format
        userData = {
          token: response.id.toString(),
          user: response,
          type: "Bearer"
        };
        userRole = response.role;
        console.log("🔄 Using old format");
      } else {
        console.log("❌ Unexpected response format:", response);
        throw new Error("Unexpected response format from server");
      }
      
      login(userData);
      
      // Redirect based on role
      setTimeout(() => {
        console.log("🚀 Login successful, redirecting to:", userRole === "ADMIN" ? "/admin" : "/dashboard");
        if (userRole === "ADMIN") {
          navigate("/admin");
        } else {
          navigate("/dashboard");
        }
      }, 100);
    } catch (err) {
      console.error("❌ Login error details:", err);
      console.error("❌ Error status:", err?.status);
      console.error("❌ Error data:", err?.data);
      
      // Better error messages
      if (err?.status === 404) {
        setError("Login endpoint not found. The server might not have the /auth/login endpoint implemented.");
      } else if (err?.status === 500) {
        setError("Server error. Please try again later.");
      } else if (err?.status === 401) {
        setError("Invalid username or password.");
      } else if (err?.message?.includes("Failed to fetch")) {
        setError("Cannot connect to server. Make sure the backend is running on port 8080.");
      } else {
        setError(err?.data?.message || err?.message || "Login failed. Please check your credentials.");
      }
    } finally {
      setLoading(false);
    }
  };

  // ... rest of your JSX remains the same ...
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