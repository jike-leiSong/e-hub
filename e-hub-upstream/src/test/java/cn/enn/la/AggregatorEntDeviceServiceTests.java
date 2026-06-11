package cn.sl.ehub.upstream;

import cn.sl.ehub.common.service.AggregatorEntDeviceService;
import cn.sl.ehub.common.vo.AggregatorEntDevice;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AggregatorEntDeviceServiceTests {

    @Resource
    private AggregatorEntDeviceService aggregatorEntDeviceService;

    @Test
    public void testGetAggregatorEntDeviceList() {
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
        aggregatorEntDeviceList.forEach(aggregatorEntDevice ->
                System.out.println(JSONObject.toJSONString(aggregatorEntDevice))
        );
    }
}
