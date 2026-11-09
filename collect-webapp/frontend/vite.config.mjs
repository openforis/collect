import fs from 'node:fs'
import path from 'node:path'
import { defineConfig, parseSync } from 'vite'
import react from '@vitejs/plugin-react'

const srcDir = path.resolve(import.meta.dirname, 'src')
// imports like 'utils/Labels' or 'App' resolve from src (CRA baseUrl, see jsconfig.json)
const srcEntries = fs.readdirSync(srcDir).map((name) => name.replace(/\.jsx?$/, ''))

// CRA's Babel (flow preset) dropped class fields declared without a value. The models rely on it:
// Serializable fills `this` in its constructor, and a native `id` field declaration in the subclass would reset it to undefined.
// Blank them out with spaces so line/column positions (and source maps) stay unchanged.
const stripBareClassFields = {
  name: 'strip-bare-class-fields',
  enforce: 'pre',
  transform(code, id) {
    const file = id.split('?')[0]
    if (!file.startsWith(srcDir) || !/\.jsx?$/.test(file)) return
    const ranges = []
    const visit = (node) => {
      if (!node || typeof node !== 'object') return
      if (Array.isArray(node)) return node.forEach(visit)
      if (node.type === 'PropertyDefinition' && node.value === null && !node.decorators?.length) ranges.push(node)
      else Object.values(node).forEach(visit)
    }
    visit(parseSync(file, code, { lang: 'jsx' }).program)
    if (!ranges.length) return
    let out = code
    for (const { start, end } of ranges) {
      const field = code.slice(start, end)
      if (!/^(static\s+)?#?[\w$]+\s*;?$/.test(field)) throw new Error(`${file}: unexpected class field ${field}`)
      out = out.slice(0, start) + ' '.repeat(field.length) + out.slice(end)
    }
    return { code: out, map: null }
  },
}

// dev: served at /collect/ like the webapp, on 127.0.0.1 (the only origin CollectCorsFilter allows)
export default defineConfig(({ command }) => ({
  base: command === 'serve' ? '/collect/' : './',
  envPrefix: 'REACT_APP_',
  plugins: [stripBareClassFields, react()],
  // sockjs-client (via react-stomp) expects Node's `global`
  define: { global: 'globalThis' },
  resolve: {
    alias: [{ find: new RegExp(`^(${srcEntries.join('|')})(?=/|$)`), replacement: `${srcDir}/$1` }],
  },
  server: { host: '127.0.0.1', port: 3000, strictPort: true },
  build: { outDir: 'build', assetsDir: 'static' },
}))
