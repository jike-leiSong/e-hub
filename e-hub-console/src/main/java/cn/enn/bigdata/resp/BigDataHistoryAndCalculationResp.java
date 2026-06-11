package cn.enn.bigdata.resp;

import cn.sl.ehub.common.vo.DataResp;
import lombok.Data;
import java.util.List;

@Data
public class BigDataHistoryAndCalculationResp {
    private String deviceId;
    private String metric;
    private List<DataResp> dataResp;
}
