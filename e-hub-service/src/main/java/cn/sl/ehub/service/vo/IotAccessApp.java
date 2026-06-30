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
@ApiModel("IoT三方接入应用")
@Table(name = "iot_access_app")
public class IotAccessApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("三方来源名称")
    @Column(name = "source_name")
    private String sourceName;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("默认项目编码")
    @Column(name = "project_id")
    private String projectId;

    @ApiModelProperty("X-GW-AccessKey")
    @Column(name = "access_key")
    private String accessKey;

    @ApiModelProperty("userKey")
    @Column(name = "user_key")
    private String userKey;

    @ApiModelProperty("是否启用")
    @Column(name = "enabled")
    private Integer enabled;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
