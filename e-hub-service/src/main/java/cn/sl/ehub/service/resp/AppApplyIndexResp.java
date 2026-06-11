package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 调峰辅助服务首页
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("调峰辅助服务首页")
public class AppApplyIndexResp {

    @ApiModelProperty("状态 0=初始页面，1=今日已申报/明日计划已提交，2=今日申报已结束，3=明日计划未提交")
    private Integer status;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("时间颜色")
    private String timeColor;
    @ApiModelProperty("申报计划")
    private String applyPlan;
    private List<AppApplyIndexDetailResp> planList;
}
