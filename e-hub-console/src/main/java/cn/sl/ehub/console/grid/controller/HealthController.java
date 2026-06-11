package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.console.service.LoadAggregatorDeliveryService;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/health")
@Api(tags = "健康检查")
public class HealthController {

    @Resource
    private LoadAggregatorDeliveryService loadAggregatorDeliveryService;

    @ApiOperation(value = "健康检查")
    @RequestMapping(value = "/getSuccess", method = RequestMethod.GET)
    public ResultVO<String> getSuccess() {
        return ResultVO.success("success");
    }

    @ApiOperation(value = "feign健康检查")
    @RequestMapping(value = "/getSuccessByFeign", method = RequestMethod.GET)
    public ResultVO<String> getSuccessByFeign() {
        return loadAggregatorDeliveryService.getSuccess();
    }
}
