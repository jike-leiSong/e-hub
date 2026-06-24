package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 峰值计划申报
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/peakPlanDeclare")
@Api(tags = "峰值计划申报")
public class PeakPlanDeclareController {

    @ApiOperation(value = "导入预测数据")
    @PostMapping("/import")
    public ResultVO<Boolean> importPredictionData(@RequestBody Object data) {
        // TODO: 实现预测数据导入逻辑
        log.info("导入预测数据: {}", data);
        return ResultVO.success(true);
    }
}
