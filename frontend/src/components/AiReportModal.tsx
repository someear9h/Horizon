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
    setSummary(null); // Clear previous summary

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
            backgroundColor: '#F5A623', // Belden-style Yellow/Orange
            color: '#111', 
            cursor: 'pointer', 
            fontWeight: 'bold',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}
        >
          ✨ Generate AI Post-Mortem
        </Button>
      </Dialog.Trigger>

      <Dialog.Content style={{ maxWidth: 550, borderRadius: '12px' }}>
        <Dialog.Title style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#222' }}>
          🤖 Horizon Intelligence: Asset #{cableId}
        </Dialog.Title>
        <Dialog.Description size="2" mb="4" style={{ color: '#666' }}>
          Automated Root-Cause Analysis via Gemini 2.5 Flash
        </Dialog.Description>

        <Flex direction="column" gap="3" style={{ minHeight: '120px', justifyContent: 'center' }}>
          {isLoading ? (
            <Flex align="center" justify="center" direction="column" gap="3">
              {/* Simple pure CSS loading spinner */}
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
              borderLeft: '5px solid #F5A623' 
            }}>
              <Text as="p" size="3" style={{ lineHeight: '1.6', color: '#111' }}>
                {summary}
              </Text>
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

      {/* Inline styles for the spinner animation */}
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