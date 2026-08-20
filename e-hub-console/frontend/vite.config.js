import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue2";
import { fileURLToPath, URL } from "node:url";
import { cpSync, existsSync, mkdirSync, readdirSync, rmSync, statSync } from "node:fs";

function syncConsoleRuntimeResources() {
  const buildOutput = fileURLToPath(new URL("../target/console-frontend-dist/", import.meta.url));
  const source = fileURLToPath(new URL("../src/main/resources/static/console/", import.meta.url));
  const targetClasses = fileURLToPath(new URL("../target/classes/", import.meta.url));
  const target = fileURLToPath(new URL("../target/classes/static/console/", import.meta.url));

  function files(directory, base = directory) {
    return readdirSync(directory).flatMap((name) => {
      const path = `${directory}/${name}`;
      if (statSync(path).isDirectory()) return files(path, base);
      return [{ name: path.slice(base.length + 1), size: statSync(path).size }];
    }).sort((left, right) => left.name.localeCompare(right.name));
  }

  function syncAndVerify(destination) {
    rmSync(destination, { recursive: true, force: true });
    mkdirSync(destination, { recursive: true });
    cpSync(buildOutput, destination, { recursive: true, force: true });

    const expected = JSON.stringify(files(buildOutput));
    const actual = JSON.stringify(files(destination));
    if (expected !== actual || !existsSync(`${destination}/index.html`)) {
      throw new Error(`Console frontend resource sync failed: ${destination}`);
    }
  }

  return {
    name: "sync-console-runtime-resources",
    closeBundle() {
      // Build outside src/main/resources so IDE resource synchronization never observes a half-written bundle.
      syncAndVerify(source);
      // Standalone builds also refresh the classpath used by a running IDE process. During Maven clean builds,
      // target/classes does not exist yet and resources:resources copies the verified source bundle afterwards.
      if (existsSync(targetClasses)) {
        syncAndVerify(target);
      }
    },
  };
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = env.VITE_PROXY_TARGET || "http://127.0.0.1:8009";
  const consoleApiPaths = [
    "/auth",
    "/platform",
    "/tenant",
    "/console-user",
    "/permission",
    "/aggregator",
    "/ent",
    "/ent-device",
    "/entPlan",
    "/entAppPlan",
    "/model",
    "/areaDict",
    "/haomaidian",
    "/openapi",
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
    "/grid-interaction",
    "/grid-delivery-quality",
  ];

  return {
    base: "./",
    plugins: [vue(), syncConsoleRuntimeResources()],
    build: {
      outDir: "../target/console-frontend-dist",
      emptyOutDir: true,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes("node_modules")) return undefined;
            if (id.includes("/echarts/") || id.includes("/zrender/")) return "charts";
            if (id.includes("/moment/")) return "moment";
            if (id.includes("/axios/")) return "http";
            return undefined;
          },
        },
      },
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
