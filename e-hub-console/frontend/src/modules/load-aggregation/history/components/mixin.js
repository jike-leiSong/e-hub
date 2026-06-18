export default {
  methods: {
    downloadFile(response) {
      const blob = new Blob([response.data], {
        type: "application/vnd.ms-excel;chartset=utf-8",
      });
      const downLoadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      const event = new MouseEvent("click");
      a.download = this.getFileName(response);
      a.href = downLoadUrl;
      a.dispatchEvent(event);
    },
    // 处理导出文件名
    getFileName(response) {
      let fileName;
      // 需要响应设置此header暴露给外部，才能获取到
      const contentDisposition = response.headers["content-disposition"];
      if (contentDisposition) {
        // 正则获取filename的值
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = filenameRegex.exec(contentDisposition);
        if (matches != null && matches[1]) {
          fileName = matches[1].replace(/['"]/g, "");
        }
        // 通过 URLEncoder.encode(pFileName, StandardCharsets.UTF_8.name()) 加密编码的, 使用decodeURI(fileName) 解密
        fileName = decodeURI(fileName);
        // 通过 new String(pFileName.getBytes(), StandardCharsets.ISO_8859_1) 加密编码的, 使用decodeURI(escape(fileName)) 解密
      }
      return fileName;
    },
  },
};
