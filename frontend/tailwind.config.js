/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        primary: 'rgb(var(--color-primary) / <alpha-value>)',
        label: '#191919',
        hoverCard: '#f4f4f4',
        divider: 'rgba(220,220,220,0.5)',
        desc: '#808080',
        category: '#999999',
        tmplBlockText: '#1075a3',
      },
      backgroundColor: {
        tmplBlock: '#e7f0f5',
      },
      boxShadow: {
        card: '0 2px 12px 0 rgba(37,43,58,0.08)',
        panel: '0 10px 20px 0 rgba(0,0,0,0.08)',
      },
      width: {
        card: '260px',
      },
      padding: {
        expTmplPanel: '24px 32px 32px',
      },
      borderColor: {
        common: 'rgba(0,0,0,0.08)',
        text: '#c2c2c2',
      },
    },
  },
  plugins: [],
};
