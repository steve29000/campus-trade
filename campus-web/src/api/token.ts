// JWT 令牌存储（HTTP 模式用）。
const KEY = 'campus-web-token';

export function getToken(): string {
  try {
    return localStorage.getItem(KEY) ?? '';
  } catch {
    return '';
  }
}

export function setToken(t: string): void {
  try {
    localStorage.setItem(KEY, t);
  } catch {
    /* ignore */
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    /* ignore */
  }
}
