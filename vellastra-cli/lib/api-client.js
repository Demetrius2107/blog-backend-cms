import axios from 'axios';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const CONFIG_PATH = path.resolve(__dirname, '../config/default.json');

let config = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf-8'));

const client = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 动态注入 Token
client.interceptors.request.use((req) => {
  if (config.token) {
    req.headers.Authorization = `Bearer ${config.token}`;
  }
  return req;
});

/**
 * 刷新配置（init 后调用）
 */
export function reloadConfig() {
  config = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf-8'));
  client.defaults.baseURL = config.apiBaseUrl;
}

/**
 * 保存配置
 */
export function saveConfig(updates) {
  config = { ...config, ...updates };
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(config, null, 2), 'utf-8');
}

/**
 * 获取当前配置
 */
export function getConfig() {
  return { ...config };
}

/**
 * 创建文章
 * 网关路由: POST /api/article → StripPrefix=2 → vellastra-article/article
 */
export async function createArticle(data) {
  const res = await client.post('/api/article', data);
  return res.data;
}

/**
 * 更新文章
 * 网关路由: PUT /api/article/{id} → StripPrefix=2 → vellastra-article/article/{id}
 */
export async function updateArticle(id, data) {
  const res = await client.put(`/api/article/${id}`, data);
  return res.data;
}

/**
 * 发布文章
 * 网关路由: PATCH /api/article/{id}/publish → StripPrefix=2 → vellastra-article/article/{id}/publish
 */
export async function publishArticle(id) {
  const res = await client.patch(`/api/article/${id}/publish`);
  return res.data;
}

/**
 * 获取文章详情
 * 网关路由: GET /api/article/{id} → StripPrefix=2 → vellastra-article/article/{id}
 */
export async function getArticle(id) {
  const res = await client.get(`/api/article/${id}`);
  return res.data;
}

/**
 * 删除文章
 * 网关路由: DELETE /api/article/{id} → StripPrefix=2 → vellastra-article/article/{id}
 */
export async function deleteArticle(id) {
  const res = await client.delete(`/api/article/${id}`);
  return res.data;
}

/**
 * 上传图片
 * 网关路由: POST /api/file/upload/image → StripPrefix=2 → vellastra-file/file/upload/image
 * 注意：需要在 gateway 的 application.yml 中补充 vellastra-file 的路由配置
 *
 * @param {string} filePath - 本地图片绝对路径
 * @param {function} onProgress - 进度回调 (percent: number 0-100)
 */
export async function uploadImage(filePath, onProgress) {
  const FormData = (await import('form-data')).default;
  const form = new FormData();
  form.append('file', fs.createReadStream(filePath));
  const res = await client.post('/api/file/upload/image', form, {
    headers: form.getHeaders(),
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
        onProgress(percent);
      }
    },
  });
  return res.data;
}
