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

    @ApiModelProperty("属性编码")
    @Column(name = "attr_code")
    private String attrCode;

    @ApiModelProperty("属性名称")
    @Column(name = "attr_name")
    private String attrName;

    @ApiModelProperty("别名")
    @Column(name = "alias_name")
    private String aliasName;

    @ApiModelProperty("属性值")
    @Column(name = "attr_value")
    private String attrValue;

    @ApiModelProperty("单位")
    @Column(name = "attr_unit")
    private String attrUnit;

    @ApiModelProperty("属性类型")
    @Column(name = "attr_type")
    private String attrType;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    public String getParamCode() {
        return attrCode;
    }

    public void setParamCode(String paramCode) {
        this.attrCode = paramCode;
    }

    public String getParamName() {
        return attrName;
    }

    public void setParamName(String paramName) {
        this.attrName = paramName;
    }

    public String getParamValue() {
        return attrValue;
    }

    public void setParamValue(String paramValue) {
        this.attrValue = paramValue;
    }

    public String getUnit() {
        return attrUnit;
    }

    public void setUnit(String unit) {
        this.attrUnit = unit;
    }
}