package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
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

    private final IAggregatorDateHolidayService aggregatorDateHolidayService;

    @ApiOperation(value = "获取申报日期列表")
    @GetMapping("/getApplyDateList")
    public ResultVO<List<String>> getApplyDateList(
            @ApiParam(value = "聚合商ID", required = false) @RequestParam(value = "aggregatorId", required = false) String aggregatorId) {
        // date 参数不传则默认查当前日期的节假日窗口（内部已 defaultIfBlank）
        List<String> dateList = aggregatorDateHolidayService.getApplyDateList(null, true);
        return ResultVO.success(dateList);
    }
}
