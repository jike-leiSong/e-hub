package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("值线图数据")
public class ChartDataResp {

    private String time;

    private String value;

}
