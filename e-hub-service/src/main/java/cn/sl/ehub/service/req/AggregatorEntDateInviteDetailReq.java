package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 企业用户邀约请求
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户邀约请求")
public class AggregatorEntDateInviteDetailReq {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业编码")
    private String stationId;
    @ApiModelProperty("邀约人")
    private String inviteBy;
}
