package cn.sl.ehub.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * WebSocket消息实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("WebSocket消息实体")
public class WebSocketMessageDTO {

    @ApiModelProperty("消息内容")
    private String message;
    @ApiModelProperty("接收人")
    private String to;
    @ApiModelProperty("推送到企业")
    private String toEntId;
}
