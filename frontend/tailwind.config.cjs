/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: ['class'],
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
    './App.tsx',
  ],
  theme: {
    extend: {
      colors: {
        'iot-dark': '#0f111a',
        'iot-panel': '#1a1d2d',
        'iot-orange': '#ff6600',
        'iot-red': '#ff3333',
        'iot-green': '#33cc33',
        'iot-yellow': '#ffcc00',
        'iot-esg': '#00e676',
        'iot-purple': '#aa44ff',
        'iot-blue': '#4488ff',
      },
      fontFamily: {
        inter: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'glass': '0 8px 32px rgba(0, 0, 0, 0.3)',
      },
      backdropBlur: {
        'md': '12px',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
};