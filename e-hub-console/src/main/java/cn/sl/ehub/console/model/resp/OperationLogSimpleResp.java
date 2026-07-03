package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("简单操作日志响应")
public class OperationLogSimpleResp {

    @ApiModelProperty("业务类型")
    private String bizType;

    @ApiModelProperty("业务ID")
    private String bizId;

    @ApiModelProperty("操作")
    private String action;

    @ApiModelProperty("操作人")
    private String operatorName;

    @ApiModelProperty("结果")
    private String result;

    @ApiModelProperty("时间")
    private String createTime;
}
