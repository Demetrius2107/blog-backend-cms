import { defineConfig } from 'vitepress'
import fs from 'fs'
import path from 'path'

// 读取构建前拉取的站点配置（由 scripts/fetch-content.js 生成）
function loadSiteConfig() {
  const configPath = path.resolve(process.cwd(), 'posts', '_site-config.json')
  if (fs.existsSync(configPath)) {
    return JSON.parse(fs.readFileSync(configPath, 'utf-8'))
  }
  return { name: 'Vellastra', description: '星垂平野阔 · 月涌大江流' }
}

const siteConfig = loadSiteConfig()

export default defineConfig({
  title: siteConfig.name || 'Vellastra',
  description: siteConfig.description || '星垂平野阔 · 月涌大江流',
  lang: 'zh-CN',

  // 文章目录：构建前由 fetch-content.js 生成的 Markdown 文件
  srcDir: '.',
  outDir: 'dist',

  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '文章', link: '/posts/' },
    ],

    sidebar: {
      '/posts/': [
        {
          text: '全部文章',
          items: listPosts(),
        },
      ],
    },

    socialLinks: [{ icon: 'github', link: 'https://github.com/Demetrius2107' }],

    footer: {
      message: 'Built with Vellastra Content System',
      copyright: 'Copyright © 2026 wanqiu',
    },
  },
})

/**
 * 扫描 posts/ 目录生成文章侧边栏列表
 */
function listPosts() {
  const postsDir = path.resolve(process.cwd(), 'posts')
  if (!fs.existsSync(postsDir)) return []

  return fs.readdirSync(postsDir)
    .filter((f) => f.endsWith('.md') && f !== '_site-config.json')
    .map((f) => {
      const content = fs.readFileSync(path.join(postsDir, f), 'utf-8')
      const titleMatch = content.match(/^#\s+(.+)$/m)
      const title = titleMatch ? titleMatch[1] : f.replace('.md', '')
      return { text: title, link: `/posts/${f}` }
    })
}
