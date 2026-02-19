import React from 'react';
import { Zap } from 'lucide-react';
import { motion } from 'framer-motion';

interface HeaderProps {
  onNavigate?: (section: string) => void;
}

const Header: React.FC<HeaderProps> = ({ onNavigate }) => {
  const [isScrolled, setIsScrolled] = React.useState(false);

  React.useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <motion.header
      className={`sticky top-0 z-50 transition-all duration-300 ${
        isScrolled
          ? 'bg-[#0f111a]/95 backdrop-blur-md border-b border-gray-800/50'
          : 'bg-[#0f111a]/80 backdrop-blur-sm'
      }`}
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-gradient-to-br from-[#ff6600] to-[#ff8833] rounded-lg">
            <Zap className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-white font-inter">
              Horizon
            </h1>
            <p className="text-xs text-gray-400">Predictive Dashboard</p>
          </div>
        </div>

        <nav className="hidden md:flex items-center gap-8">
          <button
            onClick={() => onNavigate?.('overview')}
            className="text-gray-300 hover:text-[#ff6600] transition-colors duration-200 text-sm font-medium"
          >
            Overview
          </button>
          <button
            onClick={() => onNavigate?.('analytics')}
            className="text-gray-300 hover:text-[#ff6600] transition-colors duration-200 text-sm font-medium"
          >
            Analytics
          </button>
          <button
            onClick={() => onNavigate?.('recommendations')}
            className="text-gray-300 hover:text-[#ff6600] transition-colors duration-200 text-sm font-medium"
          >
            Recommendations
          </button>
        </nav>

        <button className="px-6 py-2 bg-gradient-to-r from-[#ff6600] to-[#ff8833] text-white rounded-lg font-medium text-sm hover:shadow-lg hover:shadow-[#ff6600]/30 transition-all duration-200 hover:scale-105">
          Connect
        </button>
      </div>
    </motion.header>
  );
};

export default Header;