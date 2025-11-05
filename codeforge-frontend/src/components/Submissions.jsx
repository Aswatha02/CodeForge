import React, { useState, useEffect } from 'react';
import { userAPI, contestAPI } from '../services/api';

const Submissions = ({ problemId, contestId }) => {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSubmissions();
  }, [problemId, contestId]);

  const fetchSubmissions = async () => {
    try {
      setLoading(true);
      let response;
      
      if (contestId) {
        // Fetch contest-specific submissions for this problem
        // Note: We need to filter by problemId from all contest submissions
        const allContestSubmissions = await userAPI.getContestSubmissions(contestId);
        const problemSubmissions = allContestSubmissions.data.filter(
          sub => sub.problemId === problemId || sub.problem?.id === problemId
        );
        setSubmissions(problemSubmissions || []);
      } else {
        // Regular problem submissions
        response = await userAPI.getSubmissionsByProblem(problemId);
        setSubmissions(response.data || []);
      }
    } catch (error) {
      console.error('Error fetching submissions:', error);
      setSubmissions([]);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACCEPTED': return 'text-green-600 bg-green-100';
      case 'WRONG_ANSWER': return 'text-red-600 bg-red-100';
      case 'TIME_LIMIT_EXCEEDED': return 'text-orange-600 bg-orange-100';
      case 'RUNTIME_ERROR': return 'text-red-600 bg-red-100';
      case 'COMPILATION_ERROR': return 'text-purple-600 bg-purple-100';
      case 'PENDING': return 'text-blue-600 bg-blue-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="p-4 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-2 text-gray-600">Loading submissions...</p>
      </div>
    );
  }

  // Don't render anything if no submissions
  if (submissions.length === 0) {
    return null;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium text-white">Recent Submissions</h3>
      
      <div className="space-y-2">
        {submissions.map((submission) => (
          <div key={submission.id} className="bg-gray-800 rounded-lg p-4 border border-gray-600">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center space-x-3">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(submission.status)}`}>
                    {submission.status?.replace('_', ' ')}
                  </span>
                  <span className="text-sm text-gray-400">{submission.language}</span>
                </div>
                
                {submission.errorMessage && (
                  <div className="mt-2 text-sm text-red-400">
                    Error: {submission.errorMessage}
                  </div>
                )}
                
                {submission.passedTestCases !== undefined && submission.totalTestCases !== undefined && (
                  <div className="mt-1 text-sm text-gray-400">
                    Test Cases: {submission.passedTestCases}/{submission.totalTestCases} passed
                  </div>
                )}
              </div>
              
              <div className="text-right text-sm text-gray-400">
                <div>{formatDate(submission.submittedAt)}</div>
                {submission.executionTime && (
                  <div>{submission.executionTime}ms</div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Submissions;