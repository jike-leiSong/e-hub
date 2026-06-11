package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

/**
 * 首页总览曲线颜色
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@ToString
@ApiModel("首页总览曲线颜色")
public class IndexOverviewTimeColorResp {

    @ApiModelProperty("时间")
    private String xAxis;

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }
}
