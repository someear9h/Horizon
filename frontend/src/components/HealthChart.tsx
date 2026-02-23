import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { motion } from 'framer-motion';
import { ArrowRight } from 'lucide-react';
import type { HistoryRecord } from '../hooks/useDashboardData';

interface HealthChartProps {
  data: HistoryRecord[];
  loading: boolean;
}

const HealthChart: React.FC<HealthChartProps> = ({ data, loading }) => {
  // ✅ FIX: Removed .slice(-20) so the graph renders the complete history timeline.
  // We keep .reverse() so the oldest data is on the left and newest is on the right.
  const chartData = [...data]
    .reverse() 
    .map((record, index) => ({
      dayLabel: `Day ${(index + 1) * 6}`, // Multiplied by 6 to match the virtual days scale
      health: Math.round(record.health),
    }));

  return (
    <motion.div
      className="bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg overflow-hidden shadow-2xl"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.1 }}
    >
      <div className="p-4 border-b border-gray-800/50 flex justify-between items-center">
        <div>
          <h2 className="text-lg font-bold text-white font-inter text-left">Lifecycle Degradation</h2>
          <p className="text-xs text-gray-400 mt-1 text-left">Accelerated Aging Simulation [1s = 1 Day]</p>
        </div>
        <div className="text-[10px] bg-orange-900/30 text-[#ff6600] px-2 py-1 rounded border border-[#ff6600]/30 font-mono font-bold uppercase">
          Time Compression Active
        </div>
      </div>
      
      <div className="p-4 pb-2">
        {(!loading || chartData.length > 0) ? (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData} margin={{ bottom: 10 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#333333" vertical={false} />
              <XAxis 
                dataKey="dayLabel" 
                hide={true} 
              />
              <YAxis 
                stroke="#666666" 
                style={{ fontSize: '11px' }} 
                domain={[0, 100]} 
                tickCount={6}
                axisLine={false}
              />
              <Tooltip
                contentStyle={{ backgroundColor: '#1a1d2d', border: '1px solid #4488ff', borderRadius: '8px' }}
                itemStyle={{ color: '#ff6600', fontWeight: 'bold' }}
                labelStyle={{ color: '#4488ff', fontWeight: 'bold' }}
                formatter={(value: number) => [`${value}%`, 'Health Status']}
              />
              <Line
                type="monotone"
                dataKey="health"
                stroke="#ff6600"
                strokeWidth={4}
                dot={false}
                activeDot={{ r: 6, stroke: '#ffffff', strokeWidth: 2 }}
                isAnimationActive={false}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-64 flex items-center justify-center text-gray-500 font-mono uppercase tracking-widest">
            Awaiting Telemetry...
          </div>
        )}
        
        <div className="flex flex-col items-center mt-2 mb-4 px-8">
          <div className="w-full flex items-center opacity-60">
            <div className="h-[1px] flex-1 bg-gradient-to-r from-transparent via-gray-500 to-gray-500"></div>
            <ArrowRight className="text-gray-500 w-4 h-4 -ml-1" />
          </div>
          <div className="flex justify-between w-full mt-1 text-[10px] text-gray-500 font-mono uppercase tracking-widest">
            <span>Start of Cycle</span>
            <span>Simulation Progress (Days)</span>
            <span>End of Cycle</span>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default HealthChart;