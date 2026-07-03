package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("操作日志分页项响应")
public class OperationLogPageItemResp {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("业务类型")
    private String bizType;

    @ApiModelProperty("业务ID")
    private String bizId;

    @ApiModelProperty("操作")
    private String action;

    @ApiModelProperty("操作人ID")
    private String operatorUserId;

    @ApiModelProperty("操作人")
    private String operatorName;

    @ApiModelProperty("请求路径")
    private String requestPath;

    @ApiModelProperty("结果")
    private String result;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("时间")
    private String createTime;
}
