package cn.sl.ehub.console.controller.common;

import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
public class FileController {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @ApiOperation(value = "文件上传")
    @PostMapping("/uploadFile")
    public ResultVO<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultVO.fail(400, "上传文件不能为空");
        }

        try {
            // 创建上传目录
            String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
            Path uploadDir = Paths.get(uploadPath, dateDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", filename);
            result.put("originalFileName", originalFilename);
            result.put("fileSize", file.getSize());
            result.put("url", urlPrefix + "/" + dateDir + "/" + filename);
            result.put("uploadTime", new Date());

            log.info("文件上传成功: {}", filename);
            return ResultVO.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResultVO.fail(500, "文件上传失败: " + e.getMessage());
        }
    }
}
