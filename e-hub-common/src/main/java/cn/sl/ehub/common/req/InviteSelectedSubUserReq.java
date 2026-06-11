package cn.sl.ehub.common.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 新进邀约
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "新进邀约")
public class InviteSelectedSubUserReq {

    @ApiModelProperty(value = "聚合商ID")
    private String aggregatorId;
    @ApiModelProperty(value = "调节ID", required = true)
    private String orderId;
    @ApiModelProperty(value = "邀约类型 oneDayBefore日前fourHoursBefore日内realTime实时", required = true)
    private String type;
    @ApiModelProperty(value = "下发时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String orderTime;
    @ApiModelProperty(value = "调节开始时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String answerBeginTime;
    @ApiModelProperty(value = "调节结束时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String answerEndTime;
    @ApiModelProperty(value = "申报截止时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String deadline;
    @ApiModelProperty(value = "响应量", required = true)
    private Double responseValue;
    @ApiModelProperty(value = "响应子用户", required = true)
    private List<SelectedSubUser> selectedSubUserList;
}
