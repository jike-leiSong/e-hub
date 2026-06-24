package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 天气接口
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/weather")
@Api(tags = "天气信息")
public class WeatherController {

    @ApiOperation(value = "获取天气信息")
    @PostMapping("/getDayWeather")
    public ResultVO<Object> getDayWeather(@RequestBody Object params) {
        // TODO: 集成天气API服务
        // 可以调用第三方天气服务或从数据库查询
        log.info("获取天气信息: {}", params);
        return ResultVO.success(null);
    }
}
