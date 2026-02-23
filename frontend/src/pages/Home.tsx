import React, { useState } from 'react';
import { Activity, Zap, AlertTriangle, TrendingUp } from 'lucide-react';
import { motion } from 'framer-motion';
import Header from '../components/Header';
import Footer from '../components/Footer';
import CableSwitcher from '../components/CableSwitcher';
import TopologyGraph from '../components/TopologyGraph';
import HealthChart from '../components/HealthChart';
import AiReportModal from '../components/AiReportModal';
import MetricCard from '../components/MetricCard';
import RiskIndicator from '../components/RiskIndicator';
import CarbonFootprint from '../components/CarbonFootprint';
import RecommendationsList from '../components/RecommendationsList';
import useDashboardData from '../hooks/useDashboardData';

export default function Home() {
  const [currentCableId, setCurrentCableId] = useState(1);
  const dashboardData = useDashboardData(currentCableId);

  const currentHealth = dashboardData.history[0]?.health ?? 0;
  const currentRul = dashboardData.history[0]?.rulInDays ?? 0;
  const previousHealth = dashboardData.history[1]?.health ?? currentHealth;
  const healthTrend = Math.round(currentHealth - previousHealth);

  return (
    <div className="min-h-screen bg-[#0f111a] text-white font-inter">
      <Header />

      <main className="max-w-7xl mx-auto px-6 py-12">
        {/* Cable Switcher & AI Report Button */}
        <motion.div
          className="mb-12 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <CableSwitcher
            currentCableId={currentCableId}
            onCableChange={setCurrentCableId}
          />
          {/* AI Post-Mortem Generator Button */}
          <AiReportModal cableId={currentCableId} />
        </motion.div>

        {/* Key Metrics Grid */}
        <section className="mb-12">
          <motion.h2
            className="text-2xl font-bold mb-6 font-inter"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
            System Health Overview
          </motion.h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <MetricCard
              icon={Activity}
              label="Current Health"
              value={currentHealth}
              unit="%"
              status={
                currentHealth > 70
                  ? 'safe'
                  : currentHealth > 40
                    ? 'warning'
                    : 'critical'
              }
              trend={healthTrend}
              delay={0}
            />
            <MetricCard
              icon={Zap}
              label="Remaining Useful Life"
              value={currentRul}
              unit="days"
              status={
                currentRul > 180
                  ? 'safe'
                  : currentRul > 90
                    ? 'warning'
                    : 'critical'
              }
              delay={0.1}
            />
            <MetricCard
              icon={TrendingUp}
              label="Devices Online"
              value={dashboardData.graph?.nodes.length ?? 0}
              status="safe"
              delay={0.2}
            />
            <MetricCard
              icon={AlertTriangle}
              label="Active Alerts"
              value={dashboardData.recommendations.length}
              status={
                dashboardData.recommendations.length > 5
                  ? 'critical'
                  : dashboardData.recommendations.length > 2
                    ? 'warning'
                    : 'safe'
              }
              delay={0.3}
            />
          </div>
        </section>

        {/* Main Content Grid */}
        <section className="mb-12">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Column - Topology & Health */}
            <div className="lg:col-span-2 space-y-6">
              <TopologyGraph
                data={dashboardData.graph}
                loading={dashboardData.loading}
              />
              <HealthChart
                data={dashboardData.history}
                loading={dashboardData.loading}
              />
            </div>

            {/* Right Column - Risk & Carbon */}
            <div className="space-y-6">
              <RiskIndicator
                data={dashboardData.risk}
                loading={dashboardData.loading}
              />
              <CarbonFootprint
                data={dashboardData.carbon}
                loading={dashboardData.loading}
              />
            </div>
          </div>
        </section>

        {/* Recommendations Section */}
        <section className="mb-12">
          <motion.h2
            className="text-2xl font-bold mb-6 font-inter"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
            Predictive Insights
          </motion.h2>
          <RecommendationsList
            data={dashboardData.recommendations}
            loading={dashboardData.loading}
          />
        </section>

        {/* Error State */}
        {dashboardData.error && (
          <motion.div
            className="bg-[#ff3333]/10 border border-[#ff3333]/50 rounded-lg p-6 mb-12"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
          >
            <p className="text-[#ff3333] font-medium">
              Error loading dashboard data: {dashboardData.error}
            </p>
            <p className="text-gray-400 text-sm mt-2">
              Please ensure the backend API is running at{' '}
              <code className="bg-gray-800/50 px-2 py-1 rounded">
                http://localhost:8081/api/dashboard
              </code>
            </p>
          </motion.div>
        )}
      </main>

      <Footer />
    </div>
  );
}