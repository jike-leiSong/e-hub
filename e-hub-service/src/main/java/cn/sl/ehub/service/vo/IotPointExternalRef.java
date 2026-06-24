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
@ApiModel("IoT三方测点映射")
@Table(name = "iot_point_external_ref")
public class IotPointExternalRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("我方设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("我方测点ID")
    @Column(name = "point_id")
    private Long pointId;

    @ApiModelProperty("三方测点编码")
    @Column(name = "external_metric")
    private String externalMetric;

    @ApiModelProperty("三方测点名称")
    @Column(name = "external_metric_name")
    private String externalMetricName;

    @ApiModelProperty("倍率")
    @Column(name = "ratio")
    private Double ratio;

    @ApiModelProperty("偏移量")
    @Column(name = "offset_value")
    private Double offsetValue;

    @ApiModelProperty("状态：1启用，0停用")
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
