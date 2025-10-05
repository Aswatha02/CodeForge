import React, { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { getSubmissions } from "../api";

export default function Submissions() {
  const { user } = useAuth();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    const fetchSubmissions = async () => {
      if (!user) return;
      
      try {
        setLoading(true);
        const submissionsData = await getSubmissions(user.id);
        setSubmissions(submissionsData);
      } catch (error) {
        console.error('Error fetching submissions:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchSubmissions();
  }, [user]);

  const filteredSubmissions = submissions.filter(submission => {
    if (filter === 'all') return true;
    return submission.status === filter.toUpperCase();
  });

  const getStatusBadge = (status) => {
    const statusConfig = {
      'ACCEPTED': { color: '#28a745', bg: '#d4edda', text: 'Accepted' },
      'WRONG_ANSWER': { color: '#dc3545', bg: '#f8d7da', text: 'Wrong Answer' },
      'TIME_LIMIT_EXCEEDED': { color: '#ffc107', bg: '#fff3cd', text: 'Time Limit' },
      'RUNTIME_ERROR': { color: '#dc3545', bg: '#f8d7da', text: 'Runtime Error' },
      'COMPILATION_ERROR': { color: '#6c757d', bg: '#e9ecef', text: 'Compilation Error' }
    };

    const config = statusConfig[status] || { color: '#6c757d', bg: '#e9ecef', text: status };

    return (
      <span style={{
        padding: '4px 8px',
        borderRadius: '4px',
        fontSize: '12px',
        fontWeight: '600',
        background: config.bg,
        color: config.color
      }}>
        {config.text}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="loading">
          <div className="loading-spinner"></div>
          Loading submissions...
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 20px' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ 
          fontSize: '32px', 
          fontWeight: '700', 
          marginBottom: '8px',
          color: '#333'
        }}>
          My Submissions
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          View your submission history and results
        </p>
      </div>

      {/* Filters */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {['all', 'accepted', 'wrong_answer', 'time_limit_exceeded', 'runtime_error', 'compilation_error'].map(status => (
            <button
              key={status}
              onClick={() => setFilter(status)}
              style={{
                padding: '8px 16px',
                background: filter === status ? '#007bff' : '#f8f9fa',
                color: filter === status ? 'white' : '#333',
                border: '1px solid #dee2e6',
                borderRadius: '6px',
                cursor: 'pointer',
                fontSize: '14px',
                textTransform: 'capitalize'
              }}
            >
              {status.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {filteredSubmissions.length === 0 ? (
        <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
          <h3 style={{ marginBottom: '12px', color: '#666' }}>No submissions found</h3>
          <p style={{ color: '#999' }}>
            {filter === 'all' 
              ? "You haven't made any submissions yet. Solve some problems to see your submissions here!"
              : `No ${filter.replace('_', ' ')} submissions found.`
            }
          </p>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Problem</th>
                <th>Language</th>
                <th>Status</th>
                <th>Time</th>
                <th>Memory</th>
                <th>Submitted</th>
              </tr>
            </thead>
            <tbody>
              {filteredSubmissions.map(submission => (
                <tr key={submission.id}>
                  <td style={{ fontWeight: '500' }}>
                    {submission.problem?.title || 'Unknown Problem'}
                  </td>
                  <td>
                    <span style={{ 
                      padding: '4px 8px',
                      background: '#e9ecef',
                      borderRadius: '4px',
                      fontSize: '12px',
                      color: '#666'
                    }}>
                      {submission.language}
                    </span>
                  </td>
                  <td>
                    {getStatusBadge(submission.status)}
                  </td>
                  <td style={{ color: '#666', fontSize: '14px' }}>
                    {submission.executionTime ? `${submission.executionTime}ms` : 'N/A'}
                  </td>
                  <td style={{ color: '#666', fontSize: '14px' }}>
                    {submission.memoryUsed ? `${submission.memoryUsed}MB` : 'N/A'}
                  </td>
                  <td style={{ color: '#666', fontSize: '14px' }}>
                    {new Date(submission.submittedAt).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}