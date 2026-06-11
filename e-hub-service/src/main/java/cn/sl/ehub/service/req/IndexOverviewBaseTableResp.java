package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * 首页总览返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("首页总览返回实体")
public class IndexOverviewBaseTableResp {

    @ApiModelProperty("日期")
    private String date;
    @ApiModelProperty("资源类型")
    private String sourceTypeName;
    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("时间-数据")
    private Map<String,Double> valueMap;

}
