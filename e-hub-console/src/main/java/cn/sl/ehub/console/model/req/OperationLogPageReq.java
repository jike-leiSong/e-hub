package cn.sl.ehub.console.model.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("操作日志分页请求")
public class OperationLogPageReq {

    @ApiModelProperty("页码")
    private Integer pageIndex = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 20;

    @ApiModelProperty("业务类型")
    private String bizType;

    @ApiModelProperty("操作人ID")
    private String operatorUserId;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;
}
