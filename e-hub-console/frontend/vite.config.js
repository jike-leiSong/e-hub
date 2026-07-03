import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue2";
import { fileURLToPath, URL } from "node:url";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = env.VITE_PROXY_TARGET || "http://127.0.0.1:8009";
  const consoleApiPaths = [
    "/auth",
    "/historyQuery",
    "/yesterday",
    "/today",
    "/tomorrow",
    "/profit",
    "/aggregatorPlan",
    "/applyPlan",
    "/entUserDetail",
    "/file",
    "/weather",
    "/peakPlanDeclare",
    "/health",
    "/product",
    "/synchronize",
    "/issue",
    "/iot",
    "/tariff",
    "/statusQuery",
  ];

  return {
    base: "./",
    plugins: [vue()],
    build: {
      outDir: "../src/main/resources/static/console",
      emptyOutDir: true,
    },
    resolve: {
      extensions: [".mjs", ".js", ".mts", ".ts", ".jsx", ".tsx", ".json", ".vue"],
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
        "platform-common-component": fileURLToPath(
          new URL("./src/platform-common-component.js", import.meta.url)
        ),
      },
    },
    server: {
      port: 5173,
      proxy: {
        ...Object.fromEntries(
          consoleApiPaths.map(path => [
            path,
            {
              target: proxyTarget,
              changeOrigin: true,
            },
          ])
        ),
        "/load-aggregator-business": {
          target: proxyTarget,
          changeOrigin: true,
        },
        "/fnw-datamining": {
          target: proxyTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
