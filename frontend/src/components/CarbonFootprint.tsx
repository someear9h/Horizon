import React from 'react';
import { Leaf, TrendingDown } from 'lucide-react';
import { motion } from 'framer-motion';
import type { CarbonData } from '../hooks/useDashboardData';

interface CarbonFootprintProps {
  data: CarbonData | null;
  loading: boolean;
}

const CarbonFootprint: React.FC<CarbonFootprintProps> = ({ data, loading }) => {
  if (loading && !data) {
    return (
      <div className="bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg p-6 shadow-2xl animate-pulse space-y-4">
          <div className="h-4 bg-gray-700 rounded w-1/3" />
          <div className="h-8 bg-gray-700 rounded w-1/2" />
      </div>
    );
  }

  if (!data) return null;

  const netCarbon = data.avoidedCarbonKg - data.embeddedCarbonKg;
  const isPositive = netCarbon > 0;

  // ✅ FIX: Map the String grade to the visual dot system safely
  const getDotCount = (rating: string | undefined) => {
    if (rating === 'A+') return 5;
    if (rating === 'B') return 3;
    if (rating === 'C') return 1;
    return 0; // Default fallback
  };

  const activeDots = getDotCount(data.sustainabilityRating?.toString());

  return (
    <motion.div
      className="bg-gradient-to-br from-[#00e676] to-[#33cc33] p-0.5 rounded-lg shadow-2xl"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="bg-[#1a1d2d] rounded-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-bold text-white font-inter">ESG Carbon Impact</h3>
          <Leaf className="w-6 h-6 text-[#00e676]" />
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-gray-800/30 rounded-lg p-4">
              <p className="text-xs text-gray-400 mb-2">Avoided Carbon</p>
              <p className="text-xl font-bold text-[#00e676] font-mono">
                {data.avoidedCarbonKg.toFixed(2)}
              </p>
              <p className="text-[10px] text-gray-500 mt-1 uppercase">kg CO₂e</p>
            </div>
            <div className="bg-gray-800/30 rounded-lg p-4">
              <p className="text-xs text-gray-400 mb-2">Embedded Carbon</p>
              <p className="text-xl font-bold text-[#ffcc00] font-mono">
                {data.embeddedCarbonKg.toFixed(2)}
              </p>
              <p className="text-[10px] text-gray-500 mt-1 uppercase">kg CO₂e</p>
            </div>
          </div>

          <div className="bg-gray-800/30 rounded-lg p-4 border border-[#00e676]/30">
            <div className="flex items-center justify-between mb-2">
              <span className="text-gray-400 text-xs uppercase tracking-tighter">Net Impact</span>
              <TrendingDown className="w-4 h-4 text-[#00e676]" />
            </div>
            <p className={`text-xl font-bold font-mono ${isPositive ? 'text-[#00e676]' : 'text-[#ffcc00]'}`}>
              {isPositive ? '+' : ''}{netCarbon.toFixed(2)} kg CO₂e
            </p>
          </div>

          <div className="pt-4 border-t border-gray-800/50">
            <div className="flex items-center justify-between">
              <span className="text-gray-400 text-sm">Sustainability Status</span>
              <div className="flex gap-1">
                {/* ✅ FIX: Iterates using the exact active dots mapped from the letter grade */}
                {[...Array(5)].map((_, i) => (
                  <div key={i} className={`w-2 h-2 rounded-full ${i < activeDots ? 'bg-[#00e676]' : 'bg-gray-700'}`} />
                ))}
              </div>
            </div>
            {/* ✅ FIX: Removed the "%" symbol and formatted neatly */}
            <p className="text-xs text-[#00e676] font-bold mt-2 uppercase tracking-widest">
              GRADE: {data.sustainabilityRating} RATED
            </p>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default CarbonFootprint;