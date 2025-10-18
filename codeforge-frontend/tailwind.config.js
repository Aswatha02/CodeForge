/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        background: '#0f172a',
        surface: '#1e293b',
        'surface-light': '#334155',
        text: {
          DEFAULT: '#f1f5f9',
          muted: '#cbd5e1',
        },
        border: '#475569',
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b',
      },
      backgroundColor: {
        'background': '#0f172a',
        'surface': '#1e293b',
        'surface-light': '#334155',
      },
      textColor: {
        'text': '#f1f5f9',
        'text-muted': '#cbd5e1',
      },
      borderColor: {
        'border': '#475569',
      },
    },
  },
  plugins: [],
}
