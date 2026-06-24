/**
 * 负荷聚合 - 运营总览模块 API
 *
 * 已切换为Console本地接口
 * 所有接口调用都转发到 console.js
 */

// 直接导出console.js中的所有内容
export * from './console.js';

// 兼容性导出
export { websocketUrl, baseUrl, accessKeyValue, uploadUrl } from './console.js';
