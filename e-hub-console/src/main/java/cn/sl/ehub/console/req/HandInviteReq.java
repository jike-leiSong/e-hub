package cn.sl.ehub.console.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 邀约请求参数实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("邀约请求参数实体")
public class HandInviteReq {

    @ApiModelProperty(value = "聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("调节ID")
    private String orderId;
    @ApiModelProperty("邀约类型 oneDayBefore日前fourHoursBefore日内realTime实时")
    private String type;
    @ApiModelProperty("下发时间 yyyy-MM-dd HH:mm:ss")
    private String orderTime;
    @ApiModelProperty("调节开始时间 yyyy-MM-dd HH:mm:ss")
    private String answerBeginTime;
    @ApiModelProperty("调节结束时间 yyyy-MM-dd HH:mm:ss")
    private String answerEndTime;
    @ApiModelProperty("申报截止时间")
    private String deadline;
    @ApiModelProperty("响应量")
    private Double responseValue;
    @ApiModelProperty("响应子用户")
    private List<String> selectedSubUser;
}
