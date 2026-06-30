package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("IoT设备类型测点元数据")
@Table(name = "iot_device_type_point_metadata")
public class IotDeviceTypePointMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备类型编码")
    @Column(name = "device_type_code")
    private String deviceTypeCode;

    @ApiModelProperty("测点编码")
    @Column(name = "property_code")
    private String propertyCode;

    @ApiModelProperty("测点名称")
    @Column(name = "property_name")
    private String propertyName;

    @ApiModelProperty("数据类型")
    @Column(name = "data_type")
    private String dataType;

    @ApiModelProperty("数据类型名称")
    @Column(name = "data_type_name")
    private String dataTypeName;

    @ApiModelProperty("值类型")
    @Column(name = "value_type")
    private String valueType;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

    @ApiModelProperty("读写权限")
    @Column(name = "read_write_role")
    private String readWriteRole;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;
}
