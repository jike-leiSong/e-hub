package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新用户信息实体
 *
 * @Author sl
 * @phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("更新用户信息实体")
public class UpdateEntReq {

    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业名称")
    private String entName;
    @ApiModelProperty("合同url地址")
    private String agreement;
    @ApiModelProperty("合同开始时间")
    private String serviceStartDate;
    @ApiModelProperty("合同结束时间")
    private String serviceEndDate;
    private List<UpdateEntDeviceReq> devices;
    private List<UpdateEntPhoneReq> phones;

    // 聚合商分成比例
    @ApiModelProperty("聚合商分成比例")
    private Double percent;
}
