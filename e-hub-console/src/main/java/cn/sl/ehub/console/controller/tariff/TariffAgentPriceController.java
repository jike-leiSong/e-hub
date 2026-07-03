package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.tariff.AgentPriceOptionsResp;
import cn.sl.ehub.service.dto.tariff.AgentPricePeriodResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.service.TariffAgentPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tariff/agent-price")
@RequiredArgsConstructor
@Api(tags = "电网代理价格")
public class TariffAgentPriceController {

    private final TariffAgentPriceService tariffAgentPriceService;

    @GetMapping("/options")
    @ApiOperation("电网代理价格查询选项")
    public ResultVO<AgentPriceOptionsResp> options(AgentPriceQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getOptions(req));
    }

    @PostMapping("/prices")
    @ApiOperation("多条件查询电网代理价格、峰谷时段")
    public ResultVO<Map<String, AgentPricePeriodResp>> prices(@RequestBody AgentPriceQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getAgentPrices(req));
    }
}
