/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        atlas: {
          bg: '#0B0D10',
          surface: '#111418',
          elevated: '#15191F',
          text: '#F2EFE7',
          muted: '#8E929A',
          accent: '#D7D2C7',
          border: 'rgba(215, 210, 199, 0.16)',
          line: 'rgba(215, 210, 199, 0.28)',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        atlas: '0 24px 80px -54px rgba(0, 0, 0, 0.8)',
      },
    },
  },
  plugins: [],
};
