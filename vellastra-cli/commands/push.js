import fs from 'fs';
import path from 'path';
import chalk from 'chalk';
import ora from 'ora';
import cliProgress from 'cli-progress';
import { parseMarkdown, scanLocalImages } from '../lib/markdown-parser.js';
import { createArticle, updateArticle, uploadImage } from '../lib/api-client.js';

/**
 * vellastra-cli push [target]
 * 推送单篇文章或批量推送目录下所有 .md 文件
 */
export async function pushCommand(target, options) {
  const targetPath = path.resolve(target);

  if (!fs.existsSync(targetPath)) {
    console.error(chalk.red(`❌ 路径不存在: ${targetPath}`));
    process.exit(1);
  }

  const stats = fs.statSync(targetPath);

  if (stats.isFile() && targetPath.endsWith('.md')) {
    await pushSingleFile(targetPath);
  } else if (stats.isDirectory()) {
    const files = fs.readdirSync(targetPath).filter(f => f.endsWith('.md'));
    if (files.length === 0) {
      console.log(chalk.yellow('⚠️  目录中没有 .md 文件'));
      return;
    }
    console.log(chalk.cyan(`\n📂 发现 ${files.length} 篇 Markdown，开始批量推送...\n`));
    for (const file of files) {
      await pushSingleFile(path.join(targetPath, file));
    }
  } else {
    console.error(chalk.red('❌ 不支持的格式，请提供 .md 文件或目录'));
    process.exit(1);
  }
}

/**
 * 推送单篇文章
 */
async function pushSingleFile(filePath) {
  const fileName = path.basename(filePath);

  try {
    // ① 解析 Markdown
    const { frontmatter, content } = parseMarkdown(filePath);
    if (!frontmatter.title) {
      console.log(chalk.yellow(`⚠️  跳过 ${fileName}：缺少 title`));
      return;
    }

    // ② 扫描并上传本地图片（带实时进度条）
    const baseDir = path.dirname(filePath);
    const localImages = scanLocalImages(content, baseDir);
    let processedContent = content;

    if (localImages.length > 0) {
      console.log(chalk.cyan(`\n📷 发现 ${localImages.length} 张本地图片，开始上传...\n`));

      const progressBar = new cliProgress.SingleBar({
        format: '  📤 {file} [{bar}] {percentage}% | {value}/{total} KB',
        barCompleteChar: '█',
        barIncompleteChar: '░',
        hideCursor: true,
        clearOnComplete: true,
      });

      for (const img of localImages) {
        // 使用绝对路径获取文件大小
        const stats = fs.statSync(img.absolute);
        const totalKB = Math.max(Math.round(stats.size / 1024), 1);

        progressBar.start(totalKB, 0, { file: path.basename(img.original) });

        const result = await uploadImage(img.absolute, (percent) => {
          const loadedKB = Math.round((percent / 100) * totalKB);
          progressBar.update(loadedKB, { file: path.basename(img.original) });
        });

        progressBar.stop();

        if (result && result.data) {
          console.log(chalk.green(`  ✅ ${path.basename(img.original)} 上传完成`));
          processedContent = processedContent.replace(img.original, result.data.url);
        }
      }

      console.log(''); // 空行换隔
    }

    // ③ 推送文章
    const spinner = ora(`📤 推送文章: ${frontmatter.title}`).start();

    const articleData = {
      title: frontmatter.title,
      content: processedContent,
      summary: frontmatter.summary,
      coverImage: frontmatter.coverImage,
      categoryId: frontmatter.category,
      // 后端 CreateArticleRequest.tags 是逗号分隔字符串，frontmatter.tags 是数组，需转换
      tags: Array.isArray(frontmatter.tags) ? frontmatter.tags.join(',') : frontmatter.tags,
      status: 0, // 草稿
    };

    const result = await createArticle(articleData);
    spinner.stop();

    // 汇总信息
    const wordCount = processedContent.length;
    const imageCount = localImages.length;
    const articleId = result.data || result.id || '(未知)';
    const statusText = chalk.dim('草稿');

    console.log('');
    console.log(chalk.green('  ✅ 推送成功'));
    console.log(chalk.bold('  文章详情:'));
    console.log(`  ${chalk.yellow('▪')} 标题:   ${chalk.white(frontmatter.title)}`);
    console.log(`  ${chalk.yellow('▪')} ID:     ${chalk.cyan(articleId)}`);
    console.log(`  ${chalk.yellow('▪')} 字数:   ${chalk.white(wordCount.toLocaleString())} 字`);
    console.log(`  ${chalk.yellow('▪')} 图片:   ${chalk.white(imageCount)} 张`);
    console.log(`  ${chalk.yellow('▪')} 状态:   ${statusText}`);
    if (frontmatter.tags && frontmatter.tags.length > 0) {
      console.log(`  ${chalk.yellow('▪')} 标签:   ${chalk.white(frontmatter.tags.join(', '))}`);
    }
    console.log(`  ${chalk.yellow('▪')} 来源:   ${chalk.dim(filePath)}`);
    console.log('');
  } catch (err) {
    console.error(chalk.red(`❌ ${fileName}: ${err.message}`));
  }
}
