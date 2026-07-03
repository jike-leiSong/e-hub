package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("配置项新增更新请求")
public class ConfigItemUpsertReq {

    @ApiModelProperty("配置键")
    private String configKey;

    @ApiModelProperty("配置名称")
    private String configName;

    @ApiModelProperty("配置值")
    private String configValue;

    @ApiModelProperty("分组")
    private String configGroup;

    @ApiModelProperty("值类型")
    private String valueType;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;
}
