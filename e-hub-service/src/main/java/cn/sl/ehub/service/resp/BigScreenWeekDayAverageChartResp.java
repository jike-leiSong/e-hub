package cn.sl.ehub.service.resp;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

/**
 * 一周日平均曲线返回结果
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("一周日平均曲线返回结果")
public class BigScreenWeekDayAverageChartResp {

    @ApiModelProperty("价格")
    private List<DataResp> offerList;
    @ApiModelProperty("功率")
    private List<DataResp> powerList;
}
