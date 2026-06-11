package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.console.grid.service.SynchronizeService;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description: 数据同步服务
 * @Author sl
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/synchronize")
@Api(tags = "数据同步服务")
public class SynchronizeController {


    @Resource
    private SynchronizeService synchronizeService;

    @ApiOperation(value = "健康检查")
    @RequestMapping(value = "/clearIssue", method = RequestMethod.GET)
    public ResultVO<Boolean> clearIssue() {
        return synchronizeService.clearIssue();
    }
}
