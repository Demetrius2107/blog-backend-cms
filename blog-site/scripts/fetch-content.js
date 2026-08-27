/**
 * Vellastra 静态站构建前内容拉取脚本
 *
 * 从后端 vellastra-article 的导出 API 拉取全部已发布文章，
 * 生成为 VitePress 可消费的 Markdown 文件（含 frontmatter）。
 *
 * 环境变量：
 *   CMS_API_URL   - 后端地址（默认 http://localhost:8080，生产环境通过 env 注入）
 *   CMS_BASE_PATH - 文章输出目录（默认 ./posts）
 *
 * 运行：npm run fetch-content
 */
import fs from 'fs';
import path from 'path';

const CMS_API_URL = process.env.CMS_API_URL || 'http://localhost:8080';
const OUTPUT_DIR = path.resolve(process.env.CMS_BASE_PATH || './posts');

async function fetchJson(endpoint) {
  const url = `${CMS_API_URL}${endpoint}`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`拉取失败 ${endpoint}: HTTP ${res.status}`);
  }
  const json = await res.json();
  if (json.code !== 200) {
    throw new Error(`拉取失败 ${endpoint}: ${json.message}`);
  }
  return json.data;
}

/**
 * 将文章数据转换为 VitePress Markdown 文件
 */
function articleToMarkdown(article) {
  const frontmatter = {
    title: article.title,
    date: article.publishTime || article.createTime,
    summary: article.summary || '',
    cover: article.coverImage || '',
    categoryId: article.categoryId || '',
    views: article.viewCount || 0,
    likes: article.likeCount || 0,
  };

  const frontmatterYaml = Object.entries(frontmatter)
    .filter(([, v]) => v !== '' && v != null)
    .map(([k, v]) => `  ${k}: ${typeof v === 'string' ? `"${v.replace(/"/g, '\\"')}"` : v}`)
    .join('\n');

  // 文章正文已是 Markdown，直接拼接
  return `---\n${frontmatterYaml}\n---\n\n# ${article.title}\n\n${article.content || ''}\n`;
}

async function main() {
  console.log('📦 开始从后端拉取已发布文章...');
  console.log(`   API: ${CMS_API_URL}`);

  // 清空输出目录
  if (fs.existsSync(OUTPUT_DIR)) {
    fs.rmSync(OUTPUT_DIR, { recursive: true });
  }
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });

  // 拉取文章列表
  const articles = await fetchJson('/api/article/export/articles');
  console.log(`   获取到 ${articles.length} 篇已发布文章`);

  // 拉取站点配置
  let siteConfig = {};
  try {
    siteConfig = await fetchJson('/api/article/export/site-config');
    console.log(`   站点: ${siteConfig.name || 'Vellastra'}`);
  } catch (e) {
    console.warn(`   ⚠️ 站点配置拉取失败（非致命）: ${e.message}`);
  }

  // 生成 Markdown 文件
  let count = 0;
  for (const article of articles) {
    // 文件名：用 ID 保证唯一，避免标题中的特殊字符
    const fileName = `article-${article.id}.md`;
    const filePath = path.join(OUTPUT_DIR, fileName);
    const markdown = articleToMarkdown(article);
    fs.writeFileSync(filePath, markdown, 'utf-8');
    count++;
  }

  // 写入站点配置（供 VitePress config 读取）
  const configPath = path.resolve(OUTPUT_DIR, '_site-config.json');
  fs.writeFileSync(configPath, JSON.stringify(siteConfig, null, 2), 'utf-8');

  console.log(`✅ 已生成 ${count} 篇文章到 ${OUTPUT_DIR}`);
}

main().catch((err) => {
  console.error('❌ 内容拉取失败:', err.message);
  process.exit(1);
});
