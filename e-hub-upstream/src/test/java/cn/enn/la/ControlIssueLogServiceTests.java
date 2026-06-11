package cn.sl.ehub.upstream;

import cn.sl.ehub.common.service.ControlIssueLogService;
import cn.sl.ehub.common.vo.ControlIssueLog;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@SpringBootTest
public class ControlIssueLogServiceTests {

    @Resource
    private ControlIssueLogService controlIssueLogService;

    @Test
    public void testGetLastLogByGroupNo(){
        ControlIssueLog lastLogByGroupNo26 = controlIssueLogService.getLastLogByGroupNo("26");
        ControlIssueLog lastLogByGroupNo27 =controlIssueLogService.getLastLogByGroupNo("27");
        System.out.println(JSONObject.toJSONString(lastLogByGroupNo26));
        System.out.println(JSONObject.toJSONString(lastLogByGroupNo27));
    }

}
