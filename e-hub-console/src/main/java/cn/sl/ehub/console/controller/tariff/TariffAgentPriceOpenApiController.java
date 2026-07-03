package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.tariff.AgentPriceAreaMenuNode;
import cn.sl.ehub.service.dto.tariff.AgentPriceOpenApiQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceOpenApiResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceOptionsResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionResp;
import cn.sl.ehub.service.service.TariffAgentPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/openapi/v1/tariff/agent")
@RequiredArgsConstructor
@Api(tags = "开放接口-代理电价")
public class TariffAgentPriceOpenApiController {

    private final TariffAgentPriceService tariffAgentPriceService;

    @GetMapping("/versions")
    @ApiOperation("查询代理电价版本")
    public ResultVO<List<AgentPriceVersionResp>> versions(AgentPriceVersionQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getVersions(req));
    }

    @GetMapping("/areas")
    @ApiOperation("查询代理电价区域三级联动")
    public ResultVO<List<AgentPriceAreaMenuNode>> areas(AgentPriceVersionQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getOpenApiAreas(req));
    }

    @GetMapping("/options")
    @ApiOperation("查询代理电价业务选项")
    public ResultVO<AgentPriceOptionsResp> options(AgentPriceQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getOpenApiOptions(req));
    }

    @PostMapping("/prices/query")
    @ApiOperation("查询代理电价")
    public ResultVO<AgentPriceOpenApiResp> prices(@RequestBody @Valid AgentPriceOpenApiQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getOpenApiAgentPrices(req));
    }
}
