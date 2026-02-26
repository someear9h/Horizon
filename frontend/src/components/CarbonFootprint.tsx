import React from 'react';
import { Leaf, TrendingDown, TrendingUp } from 'lucide-react';
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

  const netCarbon = data.avoidedCarbonKg - (data.embeddedCarbonKg + data.operationalCarbonKg);
  const isPositive = netCarbon > 0;

  // DYNAMIC RATING LOGIC: Directly maps to the A+/B/C grades from your Java Service
  const getDotCount = (rating: string | undefined) => {
    const r = rating?.toString().toUpperCase();
    if (r === 'A+') return 5;
    if (r === 'B') return 3;
    if (r === 'C') return 1;
    return 0;
  };

  const activeDots = getDotCount(data.sustainabilityRating);

  return (
    <motion.div
      className="bg-gradient-to-br from-[#00e676] to-[#33cc33] p-0.5 rounded-lg shadow-2xl"
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5 }}
    >
      <div className="bg-[#1a1d2d] rounded-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex flex-col">
            <h3 className="text-lg font-bold text-white font-inter">ESG Carbon Ledger</h3>
            <p className="text-[10px] text-gray-400 uppercase tracking-widest">Live Avoidance Analytics [cite: 20]</p>
          </div>
          <Leaf className="w-6 h-6 text-[#00e676]" />
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Avoided Carbon: High-quality hardware and predictive life extension [cite: 152] */}
            <div className="bg-gray-800/30 rounded-lg p-4 border border-white/5">
              <p className="text-xs text-gray-400 mb-2">Avoided Carbon</p>
              <p className="text-xl font-bold text-[#00e676] font-mono">
                {data.avoidedCarbonKg.toFixed(2)}
              </p>
              <p className="text-[10px] text-gray-500 mt-1 uppercase">kg CO₂e [cite: 58]</p>
            </div>
            
            {/* Operational Waste: Surges as health drops (300% energy surge logic)  */}
            <div className="bg-gray-800/30 rounded-lg p-4 border border-white/5">
              <p className="text-xs text-gray-400 mb-2">Operational Waste</p>
              <p className="text-xl font-bold text-[#ff3333] font-mono">
                {data.operationalCarbonKg.toFixed(2)}
              </p>
              <p className="text-[10px] text-gray-500 mt-1 uppercase">Surge kg CO₂e</p>
            </div>
          </div>

          {/*THE DYNAMIC NET IMPACT BOX: Changes color based on efficiency [cite: 157] */}
          <div className={`rounded-lg p-4 border transition-all duration-700 ${
            isPositive ? 'bg-green-500/10 border-[#00e676]/30' : 'bg-red-500/10 border-red-500/30'
          }`}>
            <div className="flex items-center justify-between mb-2">
              <span className="text-gray-400 text-xs uppercase font-bold tracking-widest">Net System Impact</span>
              {isPositive ? <TrendingDown className="w-4 h-4 text-[#00e676]" /> : <TrendingUp className="w-4 h-4 text-red-500" />}
            </div>
            <p className={`text-2xl font-bold font-mono ${isPositive ? 'text-[#00e676]' : 'text-red-500'}`}>
              {netCarbon.toFixed(2)} kg CO₂e
            </p>
            <p className="text-[10px] text-gray-400 uppercase mt-1">
              {isPositive ? 'Net Carbon Credit Generated' : 'Efficiency Deficit Detected'}
            </p>
          </div>

          {/* SUSTAINABILITY STATUS: The "Dots" respond to the Java Grade */}
          <div className="pt-4 border-t border-gray-800/50">
            <div className="flex items-center justify-between">
              <span className="text-gray-400 text-sm">Sustainability Status</span>
              <div className="flex gap-1">
                {[...Array(5)].map((_, i) => (
                  <div 
                    key={i} 
                    className={`w-2.5 h-2.5 rounded-full transition-all duration-1000 ${
                      i < activeDots 
                        ? 'bg-[#00e676] shadow-[0_0_10px_#00e676]' 
                        : 'bg-gray-700'
                    }`} 
                  />
                ))}
              </div>
            </div>
            <p className={`text-[10px] font-bold mt-2 uppercase tracking-[0.2em] ${
               activeDots > 3 ? 'text-[#00e676]' : activeDots > 1 ? 'text-[#ffcc00]' : 'text-red-500'
            }`}>
              GRADE: {data.sustainabilityRating} RATED [cite: 236]
            </p>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default CarbonFootprint;