package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.tariff.AgentPriceDefaultMenuResp;
import cn.sl.ehub.service.dto.tariff.AgentPricePeriodResp;
import cn.sl.ehub.service.dto.tariff.HaomaidianAgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.HaomaidianMenuReq;
import cn.sl.ehub.service.service.TariffAgentPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/haomaidian/index")
@RequiredArgsConstructor
@Api(tags = "好买电首页接口")
public class HaomaidianIndexController {

    private final TariffAgentPriceService tariffAgentPriceService;

    @PostMapping("/getDefaultMenus")
    @ApiOperation("获取默认菜单")
    public ResultVO<AgentPriceDefaultMenuResp> getDefaultMenus(@RequestBody @Valid HaomaidianMenuReq req) {
        return ResultVO.success(tariffAgentPriceService.getDefaultMenus(req));
    }

    @PostMapping("/getEnAgentPrices")
    @ApiOperation("查询代理电价及峰平谷时段")
    public ResultVO<Map<String, AgentPricePeriodResp>> getEnAgentPrices(@RequestBody @Valid HaomaidianAgentPriceQueryReq req) {
        return ResultVO.success(tariffAgentPriceService.getAgentPrices(req));
    }
}
