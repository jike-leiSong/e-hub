package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("平台操作日志")
@Table(name = "console_operation_log")
public class ConsoleOperationLog {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "biz_type")
    @ApiModelProperty("业务类型")
    private String bizType;

    @Column(name = "biz_id")
    @ApiModelProperty("业务ID")
    private String bizId;

    @Column(name = "action")
    @ApiModelProperty("操作")
    private String action;

    @Column(name = "operator_user_id")
    @ApiModelProperty("操作人ID")
    private String operatorUserId;

    @Column(name = "operator_name")
    @ApiModelProperty("操作人名称")
    private String operatorName;

    @Column(name = "request_path")
    @ApiModelProperty("请求路径")
    private String requestPath;

    @Column(name = "before_json")
    @ApiModelProperty("变更前")
    private String beforeJson;

    @Column(name = "after_json")
    @ApiModelProperty("变更后")
    private String afterJson;

    @Column(name = "result")
    @ApiModelProperty("结果")
    private String result;

    @Column(name = "error_msg")
    @ApiModelProperty("错误信息")
    private String errorMsg;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;
}
