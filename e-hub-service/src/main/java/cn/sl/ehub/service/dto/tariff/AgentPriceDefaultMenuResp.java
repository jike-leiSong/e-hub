package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("代理电价默认菜单")
public class AgentPriceDefaultMenuResp {

    @ApiModelProperty("省份区域三级联动菜单")
    private List<AgentPriceAreaMenuNode> list = new ArrayList<>();

    @ApiModelProperty("其他下拉菜单")
    private Map<String, List<String>> map = new LinkedHashMap<>();
}
