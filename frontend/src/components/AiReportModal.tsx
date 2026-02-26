import React, { useState } from 'react';
import { Button, Dialog, Flex, Text } from '@radix-ui/themes';
import { toast } from 'react-toastify';

interface AiReportModalProps {
  cableId: number;
}

const AiReportModal: React.FC<AiReportModalProps> = ({ cableId }) => {
  const [summary, setSummary] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const fetchAiReport = async () => {
    setIsLoading(true);
    setIsOpen(true);
    setSummary(null); 

    try {
      const response = await fetch(`http://localhost:8081/api/report/${cableId}`);
      if (!response.ok) {
        throw new Error('Failed to fetch AI report from backend');
      }
      
      const data = await response.json();
      setSummary(data.aiExecutiveSummary);
      toast.success('AI Post-Mortem generated successfully!');
    } catch (error) {
      console.error('Error fetching AI report:', error);
      toast.error('Failed to connect to AI Intelligence Engine.');
      setSummary('System Error: Unable to generate AI analysis at this time. Please ensure the backend and Gemini API are active.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog.Root open={isOpen} onOpenChange={setIsOpen}>
      <Dialog.Trigger>
        <Button 
          onClick={fetchAiReport} 
          size="3"
          style={{ 
            backgroundColor: '#F5A623', 
            color: '#111', 
            cursor: 'pointer', 
            fontWeight: 'bold',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}
        >
          ✨ Generate AI Post-Mortem
        </Button>
      </Dialog.Trigger>

      <Dialog.Content style={{ maxWidth: 650 /* 🚀 Widened slightly for better reading */, borderRadius: '12px' }}>
        <Dialog.Title style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#222' }}>
          🤖 Horizon Intelligence: Asset #{cableId}
        </Dialog.Title>
        <Dialog.Description size="2" mb="4" style={{ color: '#666' }}>
          Automated Root-Cause Analysis via Gemini 2.5 Flash
        </Dialog.Description>

        <Flex direction="column" gap="3" style={{ minHeight: '120px', justifyContent: 'center' }}>
          {isLoading ? (
            <Flex align="center" justify="center" direction="column" gap="3">
              <div style={{
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #F5A623',
                borderRadius: '50%',
                width: '40px',
                height: '40px',
                animation: 'spin 1s linear infinite'
              }} />
              <Text color="gray" size="2" weight="medium">
                Analyzing historical telemetry and signal decay...
              </Text>
            </Flex>
          ) : (
            <div style={{ 
              backgroundColor: '#f9fafb', 
              padding: '20px', 
              borderRadius: '8px', 
              borderLeft: '5px solid #F5A623',
              maxHeight: '400px', /* 🚀 Added scroll for longer day-by-day logs */
              overflowY: 'auto'
            }}>
              {/* Map through the line breaks so it formats cleanly */}
              {summary?.split('\n').map((paragraph, index) => {
                // If it's an empty line, just return a small spacer
                if (paragraph.trim() === '') return <div key={index} style={{ height: '8px' }} />;
                
                return (
                  <Text key={index} as="p" size="3" style={{ 
                    lineHeight: '1.6', 
                    color: '#111',
                    marginBottom: '12px',
                    fontWeight: paragraph.startsWith('Day') || paragraph.startsWith('Final Impact') ? 'bold' : 'normal' // 🚀 Make headers bold!
                  }}>
                    {paragraph}
                  </Text>
                );
              })}
            </div>
          )}
        </Flex>

        <Flex gap="3" mt="5" justify="end">
          <Dialog.Close>
            <Button variant="soft" color="gray" style={{ cursor: 'pointer' }}>
              Close Report
            </Button>
          </Dialog.Close>
        </Flex>
      </Dialog.Content>

      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </Dialog.Root>
  );
};

export default AiReportModal;