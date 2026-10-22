import { defineConfig, loadEnv, type ProxyOptions } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const demoOrigin = loadEnv(mode, process.cwd(), "SHUTTLEUP_").SHUTTLEUP_DEMO_ORIGIN;
  const configureDemoProxy: ProxyOptions["configure"] = (proxy) => {
    proxy.on("proxyReq", (proxyReq, req) => {
      // Only normalize the explicitly configured same-site demo origin.
      if (demoOrigin && req.headers.origin === demoOrigin) {
        proxyReq.setHeader("Origin", "http://localhost:5173");
      }
    });
  };

  return {
    plugins: [vue()],
    server: {
      proxy: {
        "/api/analysis": {
          target: "http://localhost:8000",
          changeOrigin: true,
          configure: configureDemoProxy,
        },
        "/api": {
          target: "http://localhost:8080",
          changeOrigin: true,
          configure: configureDemoProxy,
        },
      },
    },
    build: {
      sourcemap: true,
    },
  };
});
