package cn.enn.bigdata.resp;

import lombok.Data;
import cn.sl.ehub.common.vo.DataResp;
import java.util.List;

@Data
public class BigDataRealTimeResp {
    private String equipID;
    private String equipMK;
    private String staId;
    private String metric;
    private DataResp dataResp;
}
