package cn.enn.sms.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SmsRange {
    private String startTime;
    private String endTime;
    private String type;
    private List<String> phones;
    private String templateCode;
    private List<Map<String, String>> metrics;
}
