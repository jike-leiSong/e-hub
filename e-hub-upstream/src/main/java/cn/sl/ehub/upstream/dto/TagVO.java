package cn.sl.ehub.upstream.dto;

import lombok.Data;

/**
 * TagVO（临时替代类）
 * 原：cn.enn.bigdata.resp.TagVO
 */
@Data
public class TagVO {

    private String key;
    private String value;
    private String staId;
    private String equipMK;
    private String equipID;
}
