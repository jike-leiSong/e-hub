package cn.sl.ehub.upstream;

import cn.sl.ehub.common.job.HuabeiHeartBeatJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author sl
 * @email: ouyushan@hotmail.com
 * @date 2026-05-28
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class RdfaTimerJobTests {

    @Qualifier("huabeiHeartBeatJob")
    @Autowired
    private HuabeiHeartBeatJob huabeiHeartBeatJob;

    @Test
    public void testHuabeiHeartBeatJob() {
        huabeiHeartBeatJob.execute("test");
        log.info("执行完成");
    }
}
