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
  // Use a ref to keep track of alerts we've already shown in this session
  // so we don't spam the user while waiting for the backend to mark it as read.
  const notifiedAlerts = useRef<Set<number>>(new Set());

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
        const response = await fetch('http://localhost:8081/api/alerts');
        if (!response.ok) return;
        
        const alerts: AlertData[] = await response.json();

        alerts.forEach((alert) => {
          if (!notifiedAlerts.current.has(alert.id)) {
            // 1. Mark as notified locally
            notifiedAlerts.current.add(alert.id);

            // 2. Display the Toast Notification
            const ToastContent = () => (
              <div className="font-inter">
                <strong className="block text-sm mb-1 uppercase tracking-wider">
                  {alert.severity} ALERT - Asset #{alert.cableId}
                </strong>
                <p className="text-xs leading-relaxed opacity-90">{alert.message}</p>
              </div>
            );

            if (alert.severity === 'CRITICAL') {
              toast.error(<ToastContent />, {
                autoClose: false, // Critical alerts stay on screen until manually dismissed
                theme: 'dark',
                style: { borderLeft: '4px solid #ff3333', backgroundColor: '#1a1d2d' }
              });
            } else {
              toast.warning(<ToastContent />, {
                autoClose: 8000, // Warnings fade after 8 seconds
                theme: 'dark',
                style: { borderLeft: '4px solid #F5A623', backgroundColor: '#1a1d2d' }
              });
            }

            // 3. Tell the backend we saw it, so it removes it from the unread queue
            fetch(`http://localhost:8081/api/alerts/${alert.id}/read`, {
              method: 'POST',
            }).catch(console.error);
          }
        });
      } catch (error) {
        console.error('Failed to poll alerts:', error);
      }
    };

    // Poll the backend every 3 seconds for real-time responsiveness
    const intervalId = setInterval(fetchAlerts, 3000);
    
    // Cleanup interval on unmount
    return () => clearInterval(intervalId);
  }, []);

  return null; 
};

export default AlertNotificationCenter;