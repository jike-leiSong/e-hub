package cn.sl.ehub.upstream.dto;

import cn.sl.ehub.common.vo.DataResp;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * BigData历史数据响应（临时替代类）
 * 原：cn.enn.bigdata.resp.BigDataHistoryResp
 */
@Data
public class BigDataHistoryResp {

    private List<DataPoint> data = new ArrayList<>();
    private String equipID;
    private String equipMK;
    private String staId;
    private String metric;
    private List<DataResp> dataResp = new ArrayList<>();

    @Data
    public static class DataPoint {
        private String metric;
        private String value;
        private Long timestamp;
        private String deviceId;
        private String equipID;
        private List<DataResp> dataResp;
    }
}
