import { FlatCompat } from '@eslint/eslintrc';
import { defineConfig } from 'eslint/config';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
  // @ts-ignore
  recommendedConfig: ['eslint:recommended', 'prettier/recommended'],
});

export default defineConfig([
  {
    ignores: ['.*/', 'lib/', 'build/', 'demo/*.js', 'demo/*.css', 'module/', 'docs/', 'fuxi.js', '*.js', 'node_modules/', 'mock/', 'd_config/', 'config/'],
  },
  ...compat.config({
    rules: {},
  }),
  {
    // 💡 建议使用更精确的匹配模式，例如 **/ 表示递归匹配所有层级
    files: ['**/*.model.ts'],
    rules: {
      'max-classes-per-file': 'off',
    },
  },
]);
