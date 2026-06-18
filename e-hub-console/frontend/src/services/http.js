/**
 * axios 配置
 */

import axios from "axios";

const service = axios.create({
  // headers: {
  //   'simulate': 1,
  //   'ticket': sessionStorage.getItem('ticket') ? sessionStorage.getItem('ticket') : ''
  // },
  // timeout: 5000,
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
    newConfig.headers.ticket = ticket || "";
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
    // If config does not exist or the retry option is not set, reject
    if (!config || !config.retry) return Promise.reject(error);

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
