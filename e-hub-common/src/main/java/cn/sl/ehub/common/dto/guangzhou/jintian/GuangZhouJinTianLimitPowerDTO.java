package cn.sl.ehub.common.dto.guangzhou.jintian;

import java.util.Date;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public class GuangZhouJinTianLimitPowerDTO {

    /**
     * 广州调节唯一ID
     */
    private String orderID;

    /**
     *
     */
    private Long orderTime;

    /**
     * 需限的功率
     */
    private Integer limitPower;

    private Integer totalPower;

    private Date beginTime;

    private Date endTime;

    private String stationNo;
}
