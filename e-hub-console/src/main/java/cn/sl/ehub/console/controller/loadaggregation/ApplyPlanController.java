package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 申报计划（兼容旧接口）
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/applyPlan")
@Api(tags = "申报计划（兼容）")
public class ApplyPlanController {

    @ApiOperation(value = "获取申报日期列表")
    @GetMapping("/getApplyDateList")
    public ResultVO<List<String>> getApplyDateList(
            @ApiParam(value = "聚合商ID") @RequestParam("aggregatorId") String aggregatorId) {
        // TODO: 实现获取申报日期列表的逻辑
        // 这个接口需要根据业务逻辑实现
        return ResultVO.success(null);
    }
}
