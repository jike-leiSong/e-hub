package cn.sl.ehub.upstream.dto;

import lombok.Data;

/**
 * OpentsdbReq（临时替代类）
 * 原：cn.enn.bigdata.req.OpentsdbReq
 */
@Data
public class OpentsdbReq {

    private String metric;
    private Long start;
    private Long end;
    private TagVO tags;        // 单个TagVO对象
    private String aggregator;
    private String downsample;
}
