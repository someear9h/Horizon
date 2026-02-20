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

  // Store a stringified version of the last data to prevent unnecessary updates
  const lastDataHash = useRef<string>("");

  useEffect(() => {
    if (!containerRef.current || !data || loading) return;

    // 1. Check if data actually changed to prevent "Fluctuation"
    const currentDataHash = JSON.stringify(data);
    if (currentDataHash === lastDataHash.current) return; 
    lastDataHash.current = currentDataHash;

    // 2. Format Nodes with String IDs
    const formattedNodes = data.nodes.map((node) => ({
      id: String(node.id),
      label: node.label,
      color: {
        background: node.group === 'sensor' ? '#4488ff' : node.group === 'gateway' ? '#ff6600' : '#aa44ff',
        border: '#ffffff',
        highlight: { background: '#ffcc00', border: '#ffffff' },
      },
      font: { color: '#ffffff', face: 'Inter', size: 12 },
      shape: node.group === 'sensor' ? 'dot' : 'box',
    }));

    const formattedEdges = data.edges.map((edge) => ({
      from: String(edge.from),
      to: String(edge.to),
      color: { color: '#666666' },
    }));

    // 3. THE FIX: Differential Update (No .clear())
    if (!networkRef.current) {
      nodesDataSet.current.add(formattedNodes);
      edgesDataSet.current.add(formattedEdges);

      const options = {
        physics: {
          enabled: true,
          stabilization: { iterations: 1000 },
          barnesHut: { gravitationalConstant: -26000 }
        },
        interaction: { zoomView: true, dragView: true }
      };

      networkRef.current = new Network(containerRef.current, { 
        nodes: nodesDataSet.current, 
        edges: edgesDataSet.current 
      }, options);

      networkRef.current.on("stabilizationFinished" as any, () => {
        networkRef.current?.setOptions({ physics: { enabled: false } });
      });
    } else {
      // ✅ ONLY UPDATE. DO NOT CLEAR.
      // update() checks if the ID exists; if yes, it updates. If no, it adds.
      nodesDataSet.current.update(formattedNodes);
      edgesDataSet.current.update(formattedEdges);
    }
  }, [data, loading]);

  return (
    <motion.div className="bg-[#1a1d2d]/60 border border-gray-800 rounded-lg overflow-hidden shadow-2xl">
      <div className="p-4 border-b border-gray-800">
        <h2 className="text-lg font-bold text-white">Network Topology</h2>
        <p className="text-xs text-gray-400">Real-time [Neo4j Digital Twin]</p>
      </div>
      <div ref={containerRef} className="w-full h-96 bg-[#0f111a]" />
    </motion.div>
  );
};

export default TopologyGraph;