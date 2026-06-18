function currentENV() {
  const runtimeConfig = window.__AGGREGATION_CONFIG__ || {};
  if (runtimeConfig.env) {
    return runtimeConfig.env;
  }

  const viteEnv = import.meta.env.VITE_APP_ENV;
  if (viteEnv) {
    return viteEnv;
  }

  const host = window.location.hostname;
  const href = window.location.href;
  if (host === "localhost" || host === "127.0.0.1") {
    return "isTest";
  }
  if (href.includes(".uat")) {
    return "isUat";
  }
  if (host.includes("fat")) {
    return "isFat";
  }
  if (host.includes("test") || host.includes("dev")) {
    return "isTest";
  }
  return "isProd";
}

export default {
  currentENV,
};
