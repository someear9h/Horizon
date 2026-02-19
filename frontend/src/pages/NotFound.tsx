import React from 'react';
import { motion } from 'framer-motion';
import { Home } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#0f111a] text-white flex items-center justify-center px-6">
      <motion.div
        className="text-center"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <h1 className="text-6xl font-bold mb-4 font-inter">404</h1>
        <p className="text-xl text-gray-400 mb-8">Page not found</p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-[#ff6600] to-[#ff8833] text-white rounded-lg font-medium hover:shadow-lg hover:shadow-[#ff6600]/30 transition-all duration-200 hover:scale-105"
        >
          <Home className="w-5 h-5" />
          Back to Dashboard
        </button>
      </motion.div>
    </div>
  );
}