package cn.enn.bigdata.req;

import lombok.Data;

@Data
public class OpentsdbReq {
    private Long start;
    private Long end;
    private String metric;
    private TagVO tags;
    private String aggregator;
    private String downsample;
    private String explicitTags;
}
