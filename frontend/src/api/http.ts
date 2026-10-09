import axios from "axios";
import { useGlobalLoading } from "../composables/useGlobalLoading";
import { getAccessToken, notifyUnauthorized } from "../utils/authSession";

const { startLoading, stopLoading } = useGlobalLoading();

const http = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

http.interceptors.request.use(
  (config) => {
    startLoading();
    const token = getAccessToken();
    if (token && !["/auth/login", "/auth/register"].includes(config.url ?? "")) {
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
        && !["/auth/login", "/auth/register"].includes(error.config?.url ?? "")) {
      notifyUnauthorized();
    }
    return Promise.reject(error);
  },
);

export default http;
