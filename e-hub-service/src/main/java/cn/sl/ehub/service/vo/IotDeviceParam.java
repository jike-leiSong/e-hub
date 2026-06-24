package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@ApiModel("IoT设备参数")
@Table(name = "iot_device_param")
public class IotDeviceParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("参数编码")
    @Column(name = "param_code")
    private String paramCode;

    @ApiModelProperty("参数名称")
    @Column(name = "param_name")
    private String paramName;

    @ApiModelProperty("参数值")
    @Column(name = "param_value")
    private String paramValue;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
