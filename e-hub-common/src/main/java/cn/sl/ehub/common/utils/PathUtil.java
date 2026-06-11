package cn.sl.ehub.common.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: 文件上下载路径工具类
 * @Author sl
 * @Date 2026-05-28
 */
@Component
@ConfigurationProperties(prefix = "gps")
public class PathUtil {

    /**
     * 上传路径
     */
    private static String path;

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getPath() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getPath() + "/upload";
    }

    public static String getPath() {
        return path;
    }

    public void setPath(String path) {
        PathUtil.path = path;
    }
}
