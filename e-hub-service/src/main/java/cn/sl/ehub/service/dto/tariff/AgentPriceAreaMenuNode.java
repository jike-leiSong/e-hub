package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("代理电价区域级联节点")
public class AgentPriceAreaMenuNode {

    @ApiModelProperty("节点键值")
    private String key;

    @ApiModelProperty("节点显示值")
    private String value;

    @ApiModelProperty("子节点")
    private List<AgentPriceAreaMenuNode> children = new ArrayList<>();
}
