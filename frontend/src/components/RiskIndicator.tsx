import React from 'react';
import { AlertTriangle, Shield, Zap } from 'lucide-react';
import { motion } from 'framer-motion';
import type { RiskData } from '../hooks/useDashboardData';

interface RiskIndicatorProps {
  data: RiskData | null;
  loading: boolean;
}

const RiskIndicator: React.FC<RiskIndicatorProps> = ({ data, loading }) => {
  if (loading || !data) {
    return (
      <motion.div
        className="bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg p-6 shadow-2xl"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.2 }}
      >
        <div className="animate-pulse space-y-4">
          <div className="h-4 bg-gray-700 rounded w-1/3" />
          <div className="h-8 bg-gray-700 rounded w-1/2" />
        </div>
      </motion.div>
    );
  }

  const riskLevel =
    data.overallRiskScore > 70
      ? 'critical'
      : data.overallRiskScore > 40
        ? 'warning'
        : 'safe';

  const riskColors = {
    critical: 'from-[#ff3333] to-[#ff6600]',
    warning: 'from-[#ffcc00] to-[#ff9900]',
    safe: 'from-[#33cc33] to-[#00e676]',
  };

  return (
    <motion.div
      className={`bg-gradient-to-br ${riskColors[riskLevel]} p-0.5 rounded-lg shadow-2xl`}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
    >
      <div className="bg-[#1a1d2d] rounded-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-bold text-white font-inter">
            Risk Assessment
          </h3>
          <AlertTriangle
            className={`w-6 h-6 ${
              riskLevel === 'critical'
                ? 'text-[#ff3333]'
                : riskLevel === 'warning'
                  ? 'text-[#ffcc00]'
                  : 'text-[#33cc33]'
            }`}
          />
        </div>

        <div className="space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-gray-400 text-sm">Overall Risk Score</span>
              <span className="text-2xl font-bold text-white font-mono">
                {data.overallRiskScore}%
              </span>
            </div>
            <div className="w-full bg-gray-800/50 rounded-full h-2 overflow-hidden">
              <motion.div
                className={`h-full bg-gradient-to-r ${riskColors[riskLevel]}`}
                initial={{ width: 0 }}
                animate={{ width: `${data.overallRiskScore}%` }}
                transition={{ duration: 1, ease: 'easeOut' }}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 pt-4 border-t border-gray-800/50">
            <div className="flex items-center gap-3">
              <Shield className="w-5 h-5 text-[#4488ff]" />
              <div>
                <p className="text-xs text-gray-400">Insurance</p>
                <p className="text-sm font-bold text-white">
                  {data.insuranceStatus}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Zap className="w-5 h-5 text-[#aa44ff]" />
              <div>
                <p className="text-xs text-gray-400">Redundancy Gap</p>
                <p className="text-sm font-bold text-white">
                  {data.redundancyGap}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default RiskIndicator;