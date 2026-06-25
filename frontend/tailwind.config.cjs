/** @type {import('tailwindcss').Config} */
const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
  darkMode: 'class',
  content: ['./src/**/*.{astro,html,js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#2563eb',
        'on-primary': '#ffffff',
        canvas: '#ffffff',
        surface: '#fafafa',
        'surface-hover': '#f3f4f6',
        panel: '#ffffff',
        border: '#e5e7eb',
        'border-strong': '#d1d5db',
        'border-focus': '#2563eb',
        'text-main': '#111827',
        'text-muted': '#6b7280',
        'text-subtle': '#9ca3af',
        success: '#16a34a',
        error: '#dc2626',
        warning: '#d97706',
      },
      fontFamily: {
        sans: ['Inter', ...defaultTheme.fontFamily.sans],
      },
      boxShadow: {
        'sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        'md': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
      },
    },
  },
  plugins: [],
}
