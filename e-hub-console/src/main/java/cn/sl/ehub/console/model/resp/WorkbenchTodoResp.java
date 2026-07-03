package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("工作台待办响应")
public class WorkbenchTodoResp {

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("数量")
    private Integer count;

    @ApiModelProperty("路由key")
    private String routeKey;

    @ApiModelProperty("路由名称")
    private String routeLabel;
}
