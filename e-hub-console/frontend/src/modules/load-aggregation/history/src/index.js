// 读取当前目录及所有子目录下的vue文件
const contexts = require.context(".", false, /\.vue$/);

// vue文件的路径集合
const vues = contexts.keys();

export default vues.map(vuePath => contexts(vuePath));
