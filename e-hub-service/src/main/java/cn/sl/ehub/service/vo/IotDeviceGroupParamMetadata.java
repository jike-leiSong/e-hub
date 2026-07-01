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
@ApiModel("IoT设备组参数元数据")
@Table(name = "iot_device_group_param_metadata")
public class IotDeviceGroupParamMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备组类型编码")
    @Column(name = "device_group_type")
    private String deviceGroupType;

    @ApiModelProperty("设备组类型名称")
    @Column(name = "device_group_type_name")
    private String deviceGroupTypeName;

    @ApiModelProperty("属性编码")
    @Column(name = "attr_code")
    private String attrCode;

    @ApiModelProperty("属性名称")
    @Column(name = "attr_name")
    private String attrName;

    @ApiModelProperty("属性名称英文")
    @Column(name = "attr_name_en")
    private String attrNameEn;

    @ApiModelProperty("属性类型")
    @Column(name = "attr_type")
    private String attrType;

    @ApiModelProperty("属性单位")
    @Column(name = "attr_unit")
    private String attrUnit;

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

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;
}
