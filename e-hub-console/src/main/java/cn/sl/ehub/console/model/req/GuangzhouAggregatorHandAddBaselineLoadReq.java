package cn.sl.ehub.console.model.req;

import cn.enn.bigdata.resp.BigDataResultVO;
import cn.enn.bigdata.resp.LineDataDTO;
import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "测试基线负荷")
public class GuangzhouAggregatorHandAddBaselineLoadReq {

    /*@ApiModelProperty(value = "日期")
    private String date;*/
    @ApiModelProperty(value = "大数据")
    BigDataResultVO<List<LineDataDTO>> bigData;
}
