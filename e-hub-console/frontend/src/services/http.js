/**
 * axios 配置
 */

import axios from "axios";

const service = axios.create({
  timeout: 10000,
});
service.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";
// 设置全局的请求次数，请求的间隙
service.defaults.retry = 1;
service.defaults.retryDelay = 500;
// http request 拦截器
service.interceptors.request.use(
  config => {
    const newConfig = config;
    const ticket = sessionStorage.getItem("ticket");
    const token =
      sessionStorage.getItem("token") ||
      sessionStorage.getItem("ehub-token") ||
      sessionStorage.getItem("console-token");
    newConfig.headers.ticket = ticket || "";
    if (token) {
      newConfig.headers.token = token;
      newConfig.headers.Authorization = `Bearer ${token}`;
    }
    return newConfig;
  },
  err => {
    return Promise.reject(err);
  }
);

// http response 拦截器
service.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    const config = error.config;
    const method = String((config && config.method) || "get").toLowerCase();
    const status = error.response && error.response.status;
    const retryableMethod = ["get", "head", "options"].includes(method);
    const retryableFailure = !status || status >= 500;
    // 只自动重试幂等请求，避免创建、导入、发布等写操作被重复执行。
    if (!config || !config.retry || !retryableMethod || !retryableFailure) {
      return Promise.reject(error);
    }

    // Set the variable for keeping track of the retry count
    config.__retryCount = config.__retryCount || 0;

    // Check if we've maxed out the total number of retries
    if (config.__retryCount >= config.retry) {
      // Reject with the error
      console.log(error); // console : Error: Request failed with status code 402
      return Promise.reject(error);
    }

    // Increase the retry count
    config.__retryCount += 1;

    // Create new promise to handle exponential backoff
    const backoff = new Promise(function (resolve) {
      setTimeout(function () {
        resolve();
      }, config.retryDelay || 1);
    });

    // Return the promise in which recalls axios to retry the request
    return backoff.then(function () {
      return service(config);
    });
  }
);

export default service;
