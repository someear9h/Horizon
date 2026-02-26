import { useState, useEffect, useCallback, useRef } from 'react';

export interface GraphNode {
  id: string;
  group: string;
  label: string;
}

export interface GraphEdge {
  from: string;
  to: string;
}

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export interface HistoryRecord {
  health: number;
  rulInDays: number;
  timestamp: string;
  [key: string]: any;
}

export interface CarbonData {
  avoidedCarbonKg: number;
  operationalCarbonKg: number;
  embeddedCarbonKg: number;
  sustainabilityRating: string;
}

export interface RiskData {
  overallRiskScore: number;
  insuranceStatus: string;
  redundancyGap: number;
}

export interface Recommendation {
  category: string;
  title: string;
  description: string;
  businessImpact: string;
  confidenceScore: number;
  actionLabel: string;
}

export interface DashboardData {
  graph: GraphData | null;
  history: HistoryRecord[];
  carbon: CarbonData | null;
  risk: RiskData | null;
  recommendations: Recommendation[];
  loading: boolean;
  error: string | null;
}

const API_BASE_URL = 'http://localhost:8081/api/dashboard';

const useDashboardData = (cableId: number) => {
  const [data, setData] = useState<DashboardData>({
    graph: null,
    history: [],
    carbon: null,
    risk: null,
    recommendations: [],
    loading: true, // Initial load is true
    error: null,
  });

  // Track if we have done the initial load to prevent flickering
  const initialLoadDone = useRef(false);

  const fetchData = useCallback(async () => {
    try {
      if (!initialLoadDone.current) {
        setData((prev) => ({ ...prev, loading: true, error: null }));
      } else {
        // Just clear the error, don't trigger "loading: true"
        setData((prev) => ({ ...prev, error: null }));
      }

      const [graphRes, historyRes, carbonRes, riskRes, recommendationsRes] =
        await Promise.all([
          fetch(`${API_BASE_URL}/graph`),
          fetch(`${API_BASE_URL}/history/${cableId}`),
          fetch(`${API_BASE_URL}/carbon/${cableId}`),
          fetch(`${API_BASE_URL}/risk/${cableId}`),
          fetch(`${API_BASE_URL}/recommendations/${cableId}`),
        ]);

      if (!graphRes.ok || !historyRes.ok || !carbonRes.ok || !riskRes.ok || !recommendationsRes.ok) {
        throw new Error('Failed to fetch dashboard data');
      }

      const [graph, history, carbon, risk, recommendations] = await Promise.all([
        graphRes.json(),
        historyRes.json(),
        carbonRes.json(),
        riskRes.json(),
        recommendationsRes.json(),
      ]);

      setData({
        graph,
        history: Array.isArray(history) ? history.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()) : [],
        carbon,
        risk,
        recommendations: Array.isArray(recommendations) ? recommendations : [],
        loading: false, // Turn off loading once data arrives
        error: null,
      });
      
      initialLoadDone.current = true; // Mark initial load as finished
    } catch (err) {
      setData((prev) => ({
        ...prev,
        loading: false,
        error: err instanceof Error ? err.message : 'Unknown error',
      }));
    }
  }, [cableId]);

  useEffect(() => {
    // Reset initial load marker if the cable ID changes
    initialLoadDone.current = false; 
    fetchData();
    const interval = setInterval(fetchData, 2000);
    return () => clearInterval(interval);
  }, [fetchData]);

  return data;
};

export default useDashboardData;