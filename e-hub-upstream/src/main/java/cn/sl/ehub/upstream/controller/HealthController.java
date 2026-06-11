package cn.sl.ehub.upstream.controller;

import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/health")
@Api(tags = "健康检查")
public class HealthController {

    @ApiOperation(value = "健康检查")
    @RequestMapping(value = "/getSuccess", method = RequestMethod.GET)
    public ResultVO<String> getSuccess() {
        return ResultVO.success("success");
    }
}
