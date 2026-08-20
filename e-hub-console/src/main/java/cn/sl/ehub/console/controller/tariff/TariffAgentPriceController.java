package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.console.auth.AuthContext;
import cn.sl.ehub.console.auth.AuthUser;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.dto.tariff.AgentPriceOptionsResp;
import cn.sl.ehub.service.dto.tariff.AgentPricePeriodResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffRuleCopyReq;
import cn.sl.ehub.service.dto.tariff.TariffRuleDeleteReq;
import cn.sl.ehub.service.dto.tariff.TariffRuleDeleteResp;
import cn.sl.ehub.service.dto.tariff.TariffRuleImportReq;
import cn.sl.ehub.service.dto.tariff.TariffRulePreviewResp;
import cn.sl.ehub.service.dto.tariff.TariffRulePublishResp;
import cn.sl.ehub.service.service.TariffAgentPriceImportService;
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
    private final TariffAgentPriceImportService tariffAgentPriceImportService;

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

    @PostMapping("/import/rule/preview")
    @ApiOperation("规则化电价导入预览")
    public ResultVO<TariffRulePreviewResp> rulePreview(@RequestBody TariffRuleImportReq req) {
        fillOperator(req);
        return ResultVO.success(tariffAgentPriceImportService.preview(req));
    }

    @PostMapping("/import/rule/publish")
    @ApiOperation("规则化电价导入发布")
    public ResultVO<TariffRulePublishResp> rulePublish(@RequestBody TariffRuleImportReq req) {
        fillOperator(req);
        return ResultVO.success(tariffAgentPriceImportService.publish(req));
    }

    @PostMapping("/import/rule/delete")
    @ApiOperation("规则化电价导入物理删除")
    public ResultVO<TariffRuleDeleteResp> ruleDelete(@RequestBody TariffRuleDeleteReq req) {
        return ResultVO.success(tariffAgentPriceImportService.delete(req));
    }

    @PostMapping("/import/rule/copy")
    @ApiOperation("复制已有电价规则模板")
    public ResultVO<TariffRuleImportReq> ruleCopy(@RequestBody TariffRuleCopyReq req) {
        return ResultVO.success(tariffAgentPriceImportService.copy(req));
    }

    private void fillOperator(TariffRuleImportReq req) {
        if (req == null) {
            return;
        }
        AuthUser user = AuthContext.get();
        if (user == null) {
            return;
        }
        String operator = user.getDisplayName();
        if (operator == null || operator.trim().isEmpty()) {
            operator = user.getUsername();
        }
        req.setOperatorName(operator);
    }
}
