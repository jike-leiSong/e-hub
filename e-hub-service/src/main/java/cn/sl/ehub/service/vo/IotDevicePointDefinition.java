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
@ApiModel("IoT设备测点状态值定义")
@Table(name = "iot_device_point_definition")
public class IotDevicePointDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "device_point_id")
    private Long devicePointId;

    @ApiModelProperty("状态值")
    @Column(name = "value")
    private String value;

    @ApiModelProperty("释义")
    @Column(name = "description")
    private String description;

    @ApiModelProperty("标签")
    @Column(name = "tags")
    private String tags;

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
