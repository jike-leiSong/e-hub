package cn.sl.ehub.upstream.dto;

import cn.sl.ehub.common.vo.DataResp;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * BigData实时数据响应（临时替代类）
 * 原：cn.enn.bigdata.resp.BigDataRealTimeResp
 */
@Data
public class BigDataRealTimeResp {

    private List<DataPoint> data = new ArrayList<>();
    private String deviceId;
    private String metric;
    private String value;
    private Long timestamp;
    private String staId;
    private String equipMK;
    private String equipID;
    private List<DataResp> dataResp = new ArrayList<>();

    @Data
    public static class DataPoint {
        private String metric;
        private String value;
        private Long timestamp;
        private String deviceId;
    }
}
