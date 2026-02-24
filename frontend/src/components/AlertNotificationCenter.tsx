import React, { useEffect, useRef } from 'react';
import { toast } from 'react-toastify';

interface AlertData {
  id: number;
  cableId: number;
  severity: string;
  message: string;
  timestamp: string;
  read: boolean;
}

const AlertNotificationCenter: React.FC = () => {
  // sessionSeen tracks (cableId + severity) so we don't spam, 
  // but still allows both a Warning and a Critical per cable.
  const sessionSeen = useRef<Set<string>>(new Set());

  const handleViewImpact = (cableId: number) => {
    // Dispatch custom event for TopologyGraph [cite: 171]
    const event = new CustomEvent('focus-cable-impact', { detail: { cableId } });
    window.dispatchEvent(event);
  };

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
        // Polling the backend for unread alerts 
        const response = await fetch('http://localhost:8081/api/alerts');
        if (!response.ok) return;
        
        const alerts: AlertData[] = await response.json();

        alerts.forEach((alert) => {
          const alertKey = `${alert.cableId}-${alert.severity}`;

          if (!sessionSeen.current.has(alertKey)) {
            sessionSeen.current.add(alertKey);

            const ToastContent = () => (
              <div className="font-inter">
                <div className="flex justify-between items-start mb-1">
                  <strong className="text-sm uppercase tracking-wider">
                    {alert.severity} ALERT - Asset #{alert.cableId}
                  </strong>
                </div>
                <p className="text-xs leading-relaxed opacity-90 mb-2">{alert.message}</p>
                <button 
                  onClick={() => handleViewImpact(alert.cableId)}
                  className="text-[10px] bg-white/10 hover:bg-white/20 px-2 py-1 rounded border border-white/20 transition-colors uppercase font-bold"
                >
                  View Blast Radius
                </button>
              </div>
            );

            // Triggering Toast based on severity [cite: 19]
            if (alert.severity === 'CRITICAL') {
              toast.error(<ToastContent />, {
                autoClose: false,
                theme: 'dark',
                style: { borderLeft: '4px solid #ff3333', backgroundColor: '#1a1d2d' }
              });
            } else if (alert.severity === 'WARNING') {
              toast.warning(<ToastContent />, {
                autoClose: 8000,
                theme: 'dark',
                style: { borderLeft: '4px solid #F5A623', backgroundColor: '#1a1d2d' }
              });
            }

            // Mark as read in DB so it doesn't appear in the next poll
            fetch(`http://localhost:8081/api/alerts/${alert.id}/read`, {
              method: 'POST',
            }).catch(console.error);
          }
        });
      } catch (error) {
        console.error('Failed to poll alerts:', error);
      }
    };

    const intervalId = setInterval(fetchAlerts, 3000);
    return () => clearInterval(intervalId);
  }, []);

  return null; 
};

export default AlertNotificationCenter;