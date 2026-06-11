package cn.sl.ehub.console.model.resp;/**
 * @ProjectName: load-aggregator
 * @Package: cn.sl.ehub.upstream.model.resp
 * @ClassName: GuangzhouAggregatorDatePoliceExcelResp
 * @Author sl
 * @Description: 历史报警信息导出
 * @Date 2026-05-28
 * @Version: 1.0
 */

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *@author sl
 *@date 2026-05-28
 */
@Data
@ApiModel("历史报警信息")
public class GuangzhouAggregatorDatePoliceExcelResp {

    @ApiModelProperty("时间")
    @Excel(name = "时间",width = 20D)
    private String time;

    @ApiModelProperty("报警内容")
    @Excel(name = "描述",width = 100D)
    private String content;

    @ApiModelProperty("报警状态(0未确认,1已确认)")
    @Excel(name = "状态")
    private String status;
}