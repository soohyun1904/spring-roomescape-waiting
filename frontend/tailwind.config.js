/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        'escape-bg': '#0a0a0f',
        'escape-card': '#12121a',
        'escape-border': '#1e1e2e',
        'escape-red': '#8b0000',
        'escape-red-light': '#c0392b',
        'escape-gold': '#ffd700',
        'escape-gold-dim': '#b8860b',
      },
      fontFamily: {
        cinzel: ['Cinzel', 'serif'],
      },
      boxShadow: {
        'red-glow': '0 0 20px rgba(139, 0, 0, 0.6)',
        'gold-glow': '0 0 20px rgba(255, 215, 0, 0.4)',
      },
      keyframes: {
        flicker: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
        pulse_red: {
          '0%, 100%': { boxShadow: '0 0 10px rgba(139,0,0,0.4)' },
          '50%': { boxShadow: '0 0 30px rgba(139,0,0,0.9)' },
        },
      },
      animation: {
        flicker: 'flicker 3s ease-in-out infinite',
        pulse_red: 'pulse_red 2s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
