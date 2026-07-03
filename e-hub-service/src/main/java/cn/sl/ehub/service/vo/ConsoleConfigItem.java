package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("平台配置项")
@Table(name = "console_config_item")
public class ConsoleConfigItem {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "config_key")
    @ApiModelProperty("配置键")
    private String configKey;

    @Column(name = "config_name")
    @ApiModelProperty("配置名称")
    private String configName;

    @Column(name = "config_value")
    @ApiModelProperty("配置值")
    private String configValue;

    @Column(name = "config_group")
    @ApiModelProperty("分组")
    private String configGroup;

    @Column(name = "value_type")
    @ApiModelProperty("值类型")
    private String valueType;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

    @Column(name = "remark")
    @ApiModelProperty("备注")
    private String remark;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
