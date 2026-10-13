import axios from "axios";
import { useGlobalLoading } from "../composables/useGlobalLoading";
import { getAccessToken, getBearerToken, isAccessTokenValid, notifyUnauthorized } from "../utils/authSession";

const { startLoading, stopLoading } = useGlobalLoading();

const http = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

http.interceptors.request.use(
  (config) => {
    startLoading();
    const token = getAccessToken();
    if (!["/auth/login", "/auth/register"].includes(config.url ?? "")) {
      if (!isAccessTokenValid(token)) {
        if (!(config.method === "get" && config.url === "/auth/me")) notifyUnauthorized(token);
        return Promise.reject(new axios.CanceledError("ログイン情報が無効です。", config));
      }
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: unknown) => {
    stopLoading();
    return Promise.reject(error);
  },
);

http.interceptors.response.use(
  (response) => {
    stopLoading();
    return response;
  },
  (error: unknown) => {
    stopLoading();
    if (axios.isAxiosError(error) && error.response?.status === 401
        && !["/auth/login", "/auth/register"].includes(error.config?.url ?? "")
        // セッション検証の失敗は Guard が処理し、遷移先を保持する。
        && !(error.config?.method === "get" && error.config.url === "/auth/me")) {
      notifyUnauthorized(getBearerToken(error.config?.headers?.Authorization));
    }
    return Promise.reject(error);
  },
);

export default http;
