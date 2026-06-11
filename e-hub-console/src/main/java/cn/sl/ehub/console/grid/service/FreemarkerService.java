package cn.sl.ehub.console.grid.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.vo.ResultVO;
import com.alibaba.fastjson.JSONObject;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @Description: 模板处理类
 * @Author sl
 * @Date 2026-05-28
 */
@Service
@Slf4j
public class FreemarkerService {

    private static final String ENCODING = "UTF-8";

    public ResultVO<String> process(String templateFileName, Object data) {
        ResultVO<String> result = new ResultVO<>();
        result.setCode(StatusCode.F_A.getCode());
        result.setMsg(StatusCode.F_A.getMsg());
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
        configuration.setDefaultEncoding(ENCODING);

        StringWriter out = null;
        try {

            String pathPrefix = "/templates";
            TemplateLoader ldr = new ClassTemplateLoader(this.getClass().getClassLoader(), pathPrefix);
            configuration.setTemplateLoader(ldr);
            configuration.setClassicCompatible(true);
            Template template = configuration.getTemplate(templateFileName);
            if (template == null) {
                throw new RuntimeException("根据模版路径未找到模版信息！" + templateFileName);
            }
            out = new StringWriter();
            template.process(data, out);
            StringBuffer resultTemp = out.getBuffer();
            result.setCode(StatusCode.SUCCESS.getCode());
            result.setMsg(StatusCode.SUCCESS.getMsg());
            result.setData(result != null ? resultTemp.toString() : "");
            log.info("=============print result=============={}", JSONObject.toJSON(result));
        } catch (Exception e) {
            log.error("======process template error,templateName:{},errorMsg:{}", templateFileName, e);
            result.setCode(StatusCode.F_A.getCode());
            result.setMsg(templateFileName + "处理请求打印报文异常!" + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
