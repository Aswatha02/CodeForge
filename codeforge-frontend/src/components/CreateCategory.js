import React, { useState } from "react";
import { createCategory } from "../api";
import { useNavigate } from "react-router-dom";

export default function CreateCategory() {
  const [form, setForm] = useState({ 
    name: "", 
    description: "" 
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    if (!form.name.trim()) {
      setError("Category name is required");
      setLoading(false);
      return;
    }

    try {
      await createCategory(form);
      navigate("/categories");
    } catch (err) {
      setError(err?.data?.message || "Failed to create category");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ padding: '40px 20px', maxWidth: '600px' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ 
          fontSize: '32px', 
          fontWeight: '700', 
          marginBottom: '8px',
          color: '#333'
        }}>
          Create Category
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          Add a new problem category or topic
        </p>
      </div>

      {error && (
        <div className="error" style={{ marginBottom: '24px' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="card" style={{ padding: '32px' }}>
          <div className="form-group">
            <label className="form-label">Category Name *</label>
            <input
              type="text"
              className="form-control"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="Enter category name (e.g., Arrays, Dynamic Programming)"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-control"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="Describe this category..."
              rows="4"
            />
          </div>

          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => navigate('/categories')}
              className="btn btn-outline"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
            >
              {loading ? (
                <>
                  <div className="loading-spinner"></div>
                  Creating Category...
                </>
              ) : (
                'Create Category'
              )}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}