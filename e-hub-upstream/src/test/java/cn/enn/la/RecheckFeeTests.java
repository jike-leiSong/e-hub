package cn.sl.ehub.upstream;

import cn.sl.ehub.common.service.ClearIssueLogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RecheckFeeTests {

    @Resource
    private ClearIssueLogService clearIssueLogService;

    @Test
    public void testGetLastLogByGroupNo(){

    }

}
