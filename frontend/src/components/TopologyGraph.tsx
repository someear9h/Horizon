import React, { useEffect, useRef } from 'react';
import { Network, DataSet } from 'vis-network/standalone';
import { motion } from 'framer-motion';
import type { GraphData } from '../hooks/useDashboardData';

interface TopologyGraphProps {
  data: GraphData | null;
  loading: boolean;
}

const TopologyGraph: React.FC<TopologyGraphProps> = ({ data, loading }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const networkRef = useRef<Network | null>(null);
  const nodesDataSet = useRef<any>(new DataSet([]));
  const edgesDataSet = useRef<any>(new DataSet([]));
  const lastDataHash = useRef<string>("");

  // --- BLAST RADIUS INTERACTION: STABILIZED & ACCURATE ---
  useEffect(() => {
    const handleFocusImpact = (event: any) => {
      const { cableId } = event.detail;
      const targetId = String(cableId);

      if (networkRef.current && nodesDataSet.current) {
        // 1. CRITICAL: Stop all movement immediately so zoom is pixel-perfect
        networkRef.current.stopSimulation();

        // 2. Reset all nodes to original state to clear old highlights
        const allNodes = nodesDataSet.current.get();
        const resetNodes = allNodes.map((node: any) => ({
          id: node.id,
          color: { 
            background: node.group === 'sensor' ? '#4488ff' : node.group === 'gateway' ? '#ff6600' : '#aa44ff',
            border: '#ffffff'
          },
          size: 22,
          shadow: { enabled: false }
        }));
        nodesDataSet.current.update(resetNodes);

        // 3. Highlight the specific target node FIRST
        nodesDataSet.current.update({
          id: targetId,
          color: { 
            background: '#ff3333', 
            border: '#ffffff',
            highlight: { background: '#ff3333', border: '#ffffff' }
          },
          size: 38,
          shadow: { enabled: true, color: 'rgba(255, 51, 51, 0.9)', size: 35 }
        });

        // 4. Focus with high precision 
        // We use a slight delay to ensure the stopSimulation has taken effect
        setTimeout(() => {
          networkRef.current?.focus(targetId, {
            scale: 1.5,
            offset: { x: 0, y: 0 },
            animation: { 
              duration: 1000, 
              easingFunction: 'easeInOutQuad' 
            }
          });
        }, 50);
      }
    };

    window.addEventListener('focus-cable-impact' as any, handleFocusImpact);
    return () => window.removeEventListener('focus-cable-impact' as any, handleFocusImpact);
  }, []);

  useEffect(() => {
    if (!containerRef.current || !data || loading) return;

    const currentDataHash = JSON.stringify(data);
    if (currentDataHash === lastDataHash.current) return; 
    lastDataHash.current = currentDataHash;

    const formattedNodes = data.nodes.map((node) => ({
      id: String(node.id),
      label: node.label,
      group: node.group,
      color: {
        background: node.group === 'sensor' ? '#4488ff' : node.group === 'gateway' ? '#ff6600' : '#aa44ff',
        border: '#ffffff',
        highlight: { background: '#ffcc00', border: '#ffffff' },
      },
      font: { color: '#ffffff', face: 'Inter', size: 12, weight: '600' },
      shape: node.group === 'sensor' ? 'dot' : 'box',
      size: 22,
    }));

    const formattedEdges = data.edges.map((edge) => ({
      id: `${edge.from}-${edge.to}`,
      from: String(edge.from),
      to: String(edge.to),
      color: { color: '#444444', opacity: 0.6 },
      arrows: { to: { enabled: true, scaleFactor: 0.4 } },
      width: 2
    }));

    if (!networkRef.current) {
      nodesDataSet.current.add(formattedNodes);
      edgesDataSet.current.add(formattedEdges);

      const options = {
        physics: {
          enabled: true,
          stabilization: { iterations: 1000, updateInterval: 25 },
          barnesHut: { 
            gravitationalConstant: -30000,
            springLength: 150,
            avoidOverlap: 1 
          }
        },
        interaction: { zoomView: true, dragView: true, hover: true }
      };

      networkRef.current = new Network(containerRef.current, { 
        nodes: nodesDataSet.current, 
        edges: edgesDataSet.current 
      }, options);

      // Lock physics after initial layout to prevent "jitter"
      networkRef.current.on("stabilizationFinished" as any, () => {
        networkRef.current?.setOptions({ physics: { enabled: false } });
      });
    } else {
      nodesDataSet.current.update(formattedNodes);
      edgesDataSet.current.update(formattedEdges);
    }
  }, [data, loading]);

  return (
    <motion.div 
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-[#1a1d2d]/60 border border-gray-800 rounded-lg overflow-hidden shadow-2xl relative"
    >
      <div className="p-4 border-b border-gray-800 flex justify-between items-center bg-[#1a1d2d]/80">
        <div>
          <h2 className="text-lg font-bold text-white tracking-tight">Network Topology</h2>
          <p className="text-[10px] text-blue-400 font-mono uppercase tracking-widest">
            Neo4j Graph Twin Active [cite: 71, 154]
          </p>
        </div>
        {/* <div className="flex gap-4">
          <LegendItem color="#4488ff" label="Cables" />
          <LegendItem color="#ff6600" label="Switches" />
        </div> */}
      </div>
      
      <div ref={containerRef} className="w-full h-[400px] bg-[#0f111a]" />
      <div className="absolute inset-0 pointer-events-none border border-white/5 rounded-lg" />
    </motion.div>
  );
};

const LegendItem = ({ color, label }: { color: string, label: string }) => (
  <div className="flex items-center gap-2">
    <span className="w-2 h-2 rounded-full" style={{ backgroundColor: color }} />
    <span className="text-[10px] text-gray-400 uppercase font-bold">{label}</span>
  </div>
);

export default TopologyGraph;