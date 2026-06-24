// fetch 封装：拼基址、带 JWT、解 ApiResponse 信封、错误归一。
import { API_BASE } from './config';
import { getToken } from './token';
import { ok, fail, type ApiResponse } from './types';

async function request<T>(method: string, path: string, body?: unknown): Promise<ApiResponse<T>> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  try {
    const res = await fetch(API_BASE + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    let json: unknown = null;
    try {
      json = await res.json();
    } catch {
      // 空响应体
    }

    // 后端统一信封 { code, message, data }
    if (json && typeof json === 'object' && 'code' in json) {
      return json as ApiResponse<T>;
    }
    if (!res.ok) return fail(res.status, `HTTP ${res.status}`);
    return ok(json as T);
  } catch (e) {
    return fail(0, `网络错误：${(e as Error).message}`);
  }
}

export const http = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body),
};
