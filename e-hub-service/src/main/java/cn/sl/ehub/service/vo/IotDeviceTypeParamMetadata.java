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
@ApiModel("IoT设备类型参数元数据")
@Table(name = "iot_device_type_param_metadata")
public class IotDeviceTypeParamMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备类型编码")
    @Column(name = "device_type_code")
    private String deviceTypeCode;

    @ApiModelProperty("属性编码")
    @Column(name = "attr_code")
    private String attrCode;

    @ApiModelProperty("属性名称")
    @Column(name = "attr_name")
    private String attrName;

    @ApiModelProperty("是否必填")
    @Column(name = "required_flag")
    private Integer requiredFlag;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("示例值")
    @Column(name = "sample_value")
    private String sampleValue;
}
