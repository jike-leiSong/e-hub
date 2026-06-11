package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 资源类型返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("资源类型返回实体")
public class AggregatorResourceTypeResp {

    @ApiModelProperty("主键ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
}
