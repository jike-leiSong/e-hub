package cn.enn.bigdata.resp;

import cn.sl.ehub.common.vo.DataResp;
import lombok.Data;

import java.util.List;

@Data
public class BigDataHistoryResp {
    private String equipID;
    private String equipMK;
    private String staId;
    private String metric;
    private List<DataResp> dataResp;
}
