import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    coverage: {
      // 👇 Agrega esta línea para ignorar la basura en el reporte
      exclude: ['src/__tests__/**', 'src/**/*.mock.ts', 'src/config/**', 'src/context/']
    },
    setupFiles: './src/__tests__/setup.ts',
    reporters: ['verbose', 'html'],
    outputFile: './test-results/index.html',
  },
})
