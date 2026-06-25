// 联调开关：设置 VITE_API_BASE（如 http://localhost:8080）即走真实后端（仅已支持的域），
// 不设则全部走内存 mock，应用照常独立运行。
export const API_BASE = ((import.meta.env.VITE_API_BASE as string | undefined) ?? '').replace(/\/$/, '');
export const useHttp = API_BASE.length > 0;
