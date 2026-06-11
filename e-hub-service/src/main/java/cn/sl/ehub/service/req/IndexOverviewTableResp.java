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
public class IndexOverviewTableResp {

    @ApiModelProperty("表行数据")
   List<IndexOverviewBaseTableResp> rowDataList;

}
