import React from 'react';
import { motion } from 'framer-motion';
import type { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  icon: LucideIcon;
  label: string;
  value: string | number;
  unit?: string;
  status?: 'safe' | 'warning' | 'critical';
  trend?: number;
  delay?: number;
}

const MetricCard: React.FC<MetricCardProps> = ({
  icon: Icon,
  label,
  value,
  unit,
  status = 'safe',
  trend,
  delay = 0,
}) => {
  const statusColors = {
    safe: 'text-[#33cc33] bg-[#33cc33]/10',
    warning: 'text-[#ffcc00] bg-[#ffcc00]/10',
    critical: 'text-[#ff3333] bg-[#ff3333]/10',
  };

  const displayValue = typeof value === 'number' ? Math.round(value) : value;

  return (
    <motion.div
      className="bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg p-6 shadow-2xl hover:border-gray-700/50 transition-all duration-300"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay }}
      whileHover={{ y: -4, boxShadow: '0 20px 40px rgba(255, 102, 0, 0.1)' }}
    >
      <div className="flex items-start justify-between mb-4">
        <div className={`p-3 rounded-lg ${statusColors[status]}`}>
          <Icon className="w-6 h-6" />
        </div>
        {trend !== undefined && (
          <div
            className={`text-sm font-bold ${
              trend > 0 ? 'text-[#33cc33]' : 'text-[#ff3333]'
            }`}
          >
            {trend > 0 ? '+' : ''}{Math.round(trend)}%
          </div>
        )}
      </div>
      <p className="text-gray-400 text-sm font-medium mb-2">{label}</p>
      <div className="flex items-baseline gap-2">
        <span className="text-3xl font-bold text-white font-mono">
          {displayValue}
        </span>
        {unit && <span className="text-gray-500 text-sm">{unit}</span>}
      </div>
    </motion.div>
  );
};

export default MetricCard;