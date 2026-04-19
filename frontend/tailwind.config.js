/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            fontFamily: { sans: ['Inter', 'sans-serif'] },
            colors: {
                'vet-navy': '#003f5a',
                'vet-accent': '#0ea5e9',
                'vet-admin': '#6366f1'
            }
        },
    },
    plugins: [],
}