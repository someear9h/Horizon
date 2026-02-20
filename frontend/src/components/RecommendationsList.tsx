import React from 'react';
import { CheckCircle, AlertCircle, Lightbulb } from 'lucide-react';
import { motion } from 'framer-motion';
import type { Recommendation } from '../hooks/useDashboardData';

interface RecommendationsListProps {
  data: Recommendation[];
  loading: boolean;
}

const RecommendationsList: React.FC<RecommendationsListProps> = ({
  data,
  loading,
}) => {
  const getCategoryIcon = (category: string) => {
    switch (category.toLowerCase()) {
      case 'maintenance':
        return <CheckCircle className="w-5 h-5 text-[#4488ff]" />;
      case 'risk':
        return <AlertCircle className="w-5 h-5 text-[#ff3333]" />;
      default:
        return <Lightbulb className="w-5 h-5 text-[#ffcc00]" />;
    }
  };

  const getConfidenceColor = (score: number) => {
  if (score > 80) return 'bg-[#33cc33]/20 text-[#33cc33]'; // Changed from 0.8
  if (score > 60) return 'bg-[#ffcc00]/20 text-[#ffcc00]'; // Changed from 0.6
  return 'bg-[#ff3333]/20 text-[#ff3333]';
};

  return (
    <motion.div
      className="bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg overflow-hidden shadow-2xl"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.4 }}
    >
      <div className="p-4 border-b border-gray-800/50">
        <h2 className="text-lg font-bold text-white font-inter">
          AI Recommendations
        </h2>
        <p className="text-xs text-gray-400 mt-1">
          Predictive maintenance actions
        </p>
      </div>

      <div className="divide-y divide-gray-800/50">
        {loading ? (
          <div className="p-6 space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="animate-pulse space-y-2">
                <div className="h-4 bg-gray-700 rounded w-3/4" />
                <div className="h-3 bg-gray-700 rounded w-1/2" />
              </div>
            ))}
          </div>
        ) : data.length > 0 ? (
          data.map((rec, idx) => (
            <motion.div
              key={idx}
              className="p-4 hover:bg-gray-800/20 transition-colors duration-200"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: idx * 0.1 }}
            >
              <div className="flex gap-4">
                <div className="flex-shrink-0 mt-1">
                  {getCategoryIcon(rec.category)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2 mb-1">
                    <h3 className="text-sm font-bold text-white">
                      {rec.title}
                    </h3>
                    <span
                      className={`text-xs font-bold px-2 py-1 rounded whitespace-nowrap ${getConfidenceColor(
                        rec.confidenceScore
                      )}`}
                    >
                      {Math.round(rec.confidenceScore)}%
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mb-2">
                    {rec.description}
                  </p>
                  <div className="flex items-center justify-between">
                    <p className="text-xs text-gray-500">
                      <span className="font-medium text-gray-300">Impact:</span>{' '}
                      {rec.businessImpact}
                    </p>
                    <button className="text-xs font-bold text-[#ff6600] hover:text-[#ff8833] transition-colors duration-200">
                      {rec.actionLabel}
                    </button>
                  </div>
                </div>
              </div>
            </motion.div>
          ))
        ) : (
          <div className="p-6 text-center text-gray-400">
            No recommendations available
          </div>
        )}
      </div>
    </motion.div>
  );
};

export default RecommendationsList;