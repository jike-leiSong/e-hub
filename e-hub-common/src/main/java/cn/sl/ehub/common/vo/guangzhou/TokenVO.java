package cn.sl.ehub.common.vo.guangzhou;

import lombok.Data;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
public class TokenVO {

    /**
     * 获取token状态成功， 0表示成功
     */
    private Integer succStat;

    /**
     * 请求appkey
     */
    private String appKey;

    /**
     * 获取的token
     */
    private String accessToken;

    /**
     * token有效期
     * 单位 秒
     */
    private Integer tokenAvailableTime;
}
