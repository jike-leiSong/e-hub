package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.tariff.AgentPriceDictItemResp;
import cn.sl.ehub.service.service.TariffAgentPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/areaDict")
@RequiredArgsConstructor
@Api(tags = "代理电价字典接口")
public class AreaDictController {

    private final TariffAgentPriceService tariffAgentPriceService;

    @PostMapping("/getDictByType")
    @ApiOperation("按类型获取代理电价字典")
    public ResultVO<Map<String, List<AgentPriceDictItemResp>>> getDictByType(@RequestBody List<String> typeList) {
        return ResultVO.success(tariffAgentPriceService.getDictByType(typeList));
    }
}
