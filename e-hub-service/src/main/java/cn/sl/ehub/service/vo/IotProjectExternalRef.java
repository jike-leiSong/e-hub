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
@ApiModel("IoT三方项目映射")
@Table(name = "iot_project_external_ref")
public class IotProjectExternalRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("三方来源编码")
    @Column(name = "source_code")
    private String sourceCode;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("我方项目编码")
    @Column(name = "project_id")
    private String projectId;

    @ApiModelProperty("三方项目ID")
    @Column(name = "external_project_id")
    private String externalProjectId;

    @ApiModelProperty("三方项目名称")
    @Column(name = "external_project_name")
    private String externalProjectName;

    @ApiModelProperty("状态：1启用，0停用")
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}
