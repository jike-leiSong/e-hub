package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.service.IAggregatorInfoService;
import cn.sl.ehub.service.vo.AggregatorInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/aggregator")
@Api(tags = "聚合商信息")
public class AggregatorInfoController {

    private final IAggregatorInfoService aggregatorInfoService;
    private final LoadAggregationScopeService loadScopeService;

    @ApiOperation("聚合商列表")
    @GetMapping("/list")
    public ResultVO<List<AggregatorInfo>> listAggregators() {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(null, null);
        if (scope.isAdmin()) {
            return ResultVO.success(aggregatorInfoService.getAggregatorInfoList());
        }
        if (StringUtils.isBlank(scope.getAggregatorId())) {
            return ResultVO.success(Collections.emptyList());
        }
        AggregatorInfo aggregatorInfo = aggregatorInfoService.getAggregatorInfo(scope.getAggregatorId());
        return ResultVO.success(aggregatorInfo == null
                ? Collections.emptyList()
                : Collections.singletonList(aggregatorInfo));
    }
}
