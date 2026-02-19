import React from 'react';
import { motion } from 'framer-motion';

interface CableSwitcherProps {
  currentCableId: number;
  onCableChange: (cableId: number) => void;
}

const CableSwitcher: React.FC<CableSwitcherProps> = ({
  currentCableId,
  onCableChange,
}) => {
  const cables = [
    { id: 1, label: 'Cable A' },
    { id: 2, label: 'Cable B' },
    { id: 3, label: 'Cable C' },
  ];

  return (
    <motion.div
      className="flex gap-3 p-4 bg-[#1a1d2d]/60 backdrop-blur-md border border-gray-800/50 rounded-lg"
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      {cables.map((cable) => (
        <motion.button
          key={cable.id}
          onClick={() => onCableChange(cable.id)}
          className={`px-4 py-2 rounded-lg font-medium text-sm transition-all duration-200 ${
            currentCableId === cable.id
              ? 'bg-[#ff6600] text-white shadow-lg shadow-[#ff6600]/30'
              : 'bg-gray-800/40 text-gray-300 hover:bg-gray-800/60'
          }`}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          {cable.label}
        </motion.button>
      ))}
    </motion.div>
  );
};

export default CableSwitcher;