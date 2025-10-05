import React, { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { getUserProgress, listProblems } from "../api";

export default function UserProgress() {
  const { user } = useAuth();
  const [progress, setProgress] = useState(null);
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      if (!user) return;
      
      try {
        setLoading(true);
        const [progressData, problemsData] = await Promise.all([
          getUserProgress(user.id),
          listProblems()
        ]);
        setProgress(progressData);
        setProblems(problemsData);
      } catch (error) {
        console.error('Error fetching progress data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user]);

  const getDifficultyStats = () => {
    if (!progress) return { easy: 0, medium: 0, hard: 0 };
    
    return {
      easy: progress.solvedEasyCount || 0,
      medium: progress.solvedMediumCount || 0,
      hard: progress.solvedHardCount || 0
    };
  };

  const difficultyStats = getDifficultyStats();
  const totalSolved = difficultyStats.easy + difficultyStats.medium + difficultyStats.hard;
  const totalProblems = problems.length;

  const getProblemsByDifficulty = (difficulty) => {
    return problems.filter(p => p.difficulty === difficulty.toUpperCase());
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <div className="loading">
          <div className="loading-spinner"></div>
          Loading progress...
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
          My Progress
        </h1>
        <p style={{ color: '#666', fontSize: '16px' }}>
          Track your coding journey and achievements
        </p>
      </div>

      {/* Progress Overview */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
        gap: '20px', 
        marginBottom: '40px' 
      }}>
        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total Solved</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#28a745' }}>
            {totalSolved}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Problems</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Completion Rate</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#007bff' }}>
            {totalProblems > 0 ? Math.round((totalSolved / totalProblems) * 100) : 0}%
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Overall</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Current Streak</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#ffc107' }}>
            {progress?.currentStreak || 0}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Days</p>
        </div>

        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ fontSize: '14px', color: '#666', marginBottom: '8px' }}>Total Submissions</h3>
          <p style={{ fontSize: '32px', fontWeight: '700', color: '#6f42c1' }}>
            {progress?.totalSubmissions || 0}
          </p>
          <p style={{ fontSize: '14px', color: '#666' }}>Submissions</p>
        </div>
      </div>

      {/* Difficulty Progress */}
      <div style={{ marginBottom: '40px' }}>
        <h2 style={{ marginBottom: '24px', fontSize: '24px', fontWeight: '600' }}>
          Difficulty Breakdown
        </h2>
        
        <div style={{ display: 'grid', gap: '16px' }}>
          {['easy', 'medium', 'hard'].map(difficulty => {
            const solved = difficultyStats[difficulty];
            const total = getProblemsByDifficulty(difficulty).length;
            const percentage = total > 0 ? Math.round((solved / total) * 100) : 0;
            
            return (
              <div key={difficulty} className="card" style={{ padding: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span className={`difficulty-badge difficulty-${difficulty}`} style={{ textTransform: 'capitalize' }}>
                      {difficulty}
                    </span>
                    <span style={{ fontWeight: '600', color: '#333' }}>
                      {solved} / {total} solved
                    </span>
                  </div>
                  <span style={{ color: '#666', fontWeight: '600' }}>{percentage}%</span>
                </div>
                
                <div style={{ 
                  height: '8px', 
                  background: '#e9ecef', 
                  borderRadius: '4px',
                  overflow: 'hidden'
                }}>
                  <div 
                    style={{ 
                      height: '100%', 
                      background: 
                        difficulty === 'easy' ? '#28a745' :
                        difficulty === 'medium' ? '#ffc107' : '#dc3545',
                      width: `${percentage}%`,
                      transition: 'width 0.3s ease'
                    }}
                  ></div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Recent Activity */}
      <div>
        <h2 style={{ marginBottom: '20px', fontSize: '24px', fontWeight: '600' }}>
          Recent Activity
        </h2>
        
        <div className="card" style={{ padding: '24px', textAlign: 'center' }}>
          <h3 style={{ marginBottom: '12px', color: '#666' }}>Activity Tracking</h3>
          <p style={{ color: '#999', marginBottom: '20px' }}>
            Recent problem-solving activity will appear here
          </p>
          <p style={{ color: '#999', fontSize: '14px' }}>
            Solve more problems to see your activity history!
          </p>
        </div>
      </div>
    </div>
  );
}