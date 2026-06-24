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
@ApiModel("IoT设备测点")
@Table(name = "iot_device_point")
public class IotDevicePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("标准测点编码")
    @Column(name = "point_code")
    private String pointCode;

    @ApiModelProperty("测点名称")
    @Column(name = "point_name")
    private String pointName;

    @ApiModelProperty("值类型")
    @Column(name = "value_type")
    private String valueType;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

    @ApiModelProperty("采集频率，秒")
    @Column(name = "data_frequency")
    private Integer dataFrequency;

    @ApiModelProperty("是否核心测点")
    @Column(name = "required_flag")
    private Integer requiredFlag;

    @ApiModelProperty("读写角色")
    @Column(name = "read_write_role")
    private String readWriteRole;

    @ApiModelProperty("状态：1启用，0停用")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;

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
