import React from 'react';

const ProblemCard = ({ problem, onClick }) => {
  const getDifficultyColor = (difficulty) => {
    switch (difficulty) {
      case 'EASY': return 'text-green-600 bg-green-100';
      case 'MEDIUM': return 'text-yellow-600 bg-yellow-100';
      case 'HARD': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'solved': return '✅';
      case 'attempted': return '🟡';
      default: return '⚪';
    }
  };

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-lg shadow hover:shadow-md transition-shadow p-6 cursor-pointer border border-gray-200"
    >
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-4">
            <span className="text-lg">{getStatusIcon(problem.status)}</span>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 hover:text-blue-600">
                {problem.title}
              </h3>
              <p className="text-gray-600 text-sm mt-1 line-clamp-2">
                {problem.description}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-4 mt-3">
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(problem.difficulty)}`}>
              {problem.difficulty}
            </span>
            
            <div className="flex items-center space-x-1 text-sm text-gray-500">
              <span>💡</span>
              <span>Acceptance: {problem.acceptanceRate || 'N/A'}%</span>
            </div>

            <div className="flex items-center space-x-1 text-sm text-gray-500">
              <span>⭐</span>
              <span>Likes: {problem.likes || 0}</span>
            </div>

            {problem.categories && (
              <div className="flex items-center space-x-2">
                {problem.categories.slice(0, 2).map(category => (
                  <span
                    key={category.id}
                    className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs"
                  >
                    {category.name}
                  </span>
                ))}
                {problem.categories.length > 2 && (
                  <span className="text-xs text-gray-500">
                    +{problem.categories.length - 2} more
                  </span>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="text-right">
          <div className="text-sm text-gray-500">Premium</div>
          <div className="text-xs text-gray-400 mt-1"># {problem.problemNumber}</div>
        </div>
      </div>
    </div>
  );
};

export default ProblemCard;