package cn.sl.ehub.common.dto;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Description: token相关参数
 * @Author sl
 * @Date 2026-05-28
 */

@Data
public class TokenDTO {


    /**
     * 公钥
     * 第三方使用
     */
    private String appKey;

    /**
     * 私钥
     * 数能使用
     */
    private String appSecret;

    /**
     * 时间戳 13位
     */
    private Long timestamp;

    /**
     * 签名
     * MD5加密(appKey+appSecret+timestamp)
     */
    private String sign;

    /**
     * 头部信息
     */
    private Map<String, String> header;

    /**
     * 发布者
     */
    private String issuer;

    /**
     * 主题
     */
    private String subject;

    /**
     * 接受者
     */
    private String audience;

    /**
     * 签名时间
     */
    private Date issueAt;

    /**
     * 过期时间
     */
    private Date expiresAt;

    /**
     * 自定义参数
     */
    private Map<String, Object> claims;

    /**
     * 签名算法
     * HS256
     */
    private Algorithm algorithm;

    /**
     * 编号
     */
    private String jwtId;
}