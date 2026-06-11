package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业用户详情返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业用户详情返回实体")
public class EntUserDetailResp {

    @ApiModelProperty("企业名称")
    private String entName;
    @ApiModelProperty("经度")
    private String longitude;
    @ApiModelProperty("纬度")
    private String latitude;
    @ApiModelProperty("总功率")
    private Double totalPower;
    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业编码")
    private String stationId;
    @ApiModelProperty("聚合商占比，原企业用户占比")
    private Double percent;
    @ApiModelProperty("企业SN编码")
    private String snCode;
    @ApiModelProperty("合同url地址")
    private String agreement;
    @ApiModelProperty("合同开始时间")
    private String serviceStartDate;
    @ApiModelProperty("合同结束时间")
    private String serviceEndDate;
    @ApiModelProperty("合同期限")
    private String agreementDate;
    @ApiModelProperty("设备")
    private List<EntUserDeviceResp> devices;
    @ApiModelProperty("联系人")
    private List<EntUserPhoneResp> phones;
}
