package cn.sl.ehub.common.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * @Description: 总加数据请求参数
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel(value = "总加数据请求参数")
public class TotalDeliveryReq {

    @ApiModelProperty(value = "聚合商id")
    private String aggreratorId;

    @ApiModelProperty(value = "总加上送数据")
    private Map<String, String> cmdData;

    public TotalDeliveryReq(String aggreratorId, Map<String, String> cmdData) {
        this.aggreratorId = aggreratorId;
        this.cmdData = cmdData;
    }

    public String getAggreratorId() {
        return aggreratorId;
    }

    public void setAggreratorId(String aggreratorId) {
        this.aggreratorId = aggreratorId;
    }

    public Map<String, String> getCmdData() {
        return cmdData;
    }

    public void setCmdData(Map<String, String> cmdData) {
        this.cmdData = cmdData;
    }
}
