package cn.sl.ehub.upstream.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.upstream.service.DeliveryService;
import cn.sl.ehub.upstream.service.DeliveryServiceXinTai;
import cn.sl.ehub.upstream.service.PeakPlanDeliveryService;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 电网上送接口
 *
 * @author sl
 * @date 2026-05-28
 */
@RestController
@RequestMapping("/delivery")
@Api(tags = "电网上送接口")
@Slf4j
public class DeliveryController {

    @Resource
    private DeliveryService deliveryService;

    @Resource
    private DeliveryServiceXinTai deliveryServiceXinTai;

    @Resource
    private PeakPlanDeliveryService peakPlanDeliveryService;

    /**
     * 单体量测数据上送接口
     *
     * 单体量测的上送周期为15分钟（每小时00,15,30,45分钟上送）
     * 上送内容为前15分钟的平均值
     *
     * @param aggregatorId 聚合商ID
     * @return 上送结果
     */
    @GetMapping("/singleMeasDataDelivery")
    @ApiOperation(value = "单体量测数据上送接口", notes = "单体量测数据上送接口")
    public ResultVO<String> singleMeasDataDelivery(@RequestParam String aggregatorId) {
        return deliveryServiceXinTai.totalDataDelivery(aggregatorId);
    }

    /**
     * 单体模型数据上送接口
     *
     * 模型数据在聚合商模型发生变更时上送，上送范围为变更后的全量模型。
     *
     * @param aggregatorId 聚合商ID
     * @param energyType   能源类型（可选），中文名称，如：电采暖、工业负荷、储能、充电桩。不传则上传全部能源类型
     * @return 上送结果
     */
    @GetMapping("/singleModelDataDelivery")
    @ApiOperation(value = "单体模型数据上送接口", notes = "单体模型数据上送接口")
    public ResultVO<String> singleModelDataDelivery(@RequestParam String aggregatorId,
            @RequestParam(required = false) String energyType) {
        return ResultVO.success(deliveryService.singleModelDataDelivery(aggregatorId, energyType));
    }

    /**
     * 调峰计划申报96点数据电网上送接口
     *
     * 支持默认次日和指定日期、支持资源类型ID可选
     *
     * @param aggregatorId   聚合商ID
     * @param dataDate       数据日期（可选），格式：yyyy-MM-dd，不传则为次日
     * @param resourceTypeId 资源类型ID（可选）
     * @return 上送结果
     */
    @GetMapping("/peakPlan96PointDeliveryByDate")
    @ApiOperation(value = "调峰计划申报96点数据电网上送接口", notes = "调峰计划申报96点数据电网上送接口")
    public ResultVO<String> peakPlan96PointDeliveryByDate(@RequestParam String aggregatorId,
            @RequestParam(required = false) String dataDate,
            @RequestParam(required = false) String resourceTypeId) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date targetDate = null;
            if (org.apache.commons.lang3.StringUtils.isNotBlank(dataDate)) {
                targetDate = sdf.parse(dataDate);
            }
            boolean result = peakPlanDeliveryService.execute96PointDeliveryByDate(aggregatorId, targetDate,
                    resourceTypeId);
            return ResultVO.success("调峰计划96点数据上送成功");
        } catch (Exception e) {
            log.error("调峰计划申报96点数据电网上送接口异常", e);
            return ResultVO.fail(StatusCode.C.getCode(), e.getMessage());
        }
    }

    /**
     * 调峰计划申报日数据电网上送接口
     *
     * 支持默认次日和指定日期、支持资源类型ID可选
     *
     * @param aggregatorId   聚合商ID
     * @param dataDate       数据日期（可选），格式：yyyy-MM-dd，不传则为次日
     * @param resourceTypeId 资源类型ID（可选）
     * @return 上送结果
     */
    @GetMapping("/peakPlanDailyDataDelivery")
    @ApiOperation(value = "调峰计划申报日数据电网上送接口", notes = "调峰计划申报日数据电网上送接口")
    public ResultVO<String> peakPlanDailyDataDelivery(@RequestParam String aggregatorId,
            @RequestParam(required = false) String dataDate,
            @RequestParam(required = false) String resourceTypeId) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date targetDate = null;
            if (org.apache.commons.lang3.StringUtils.isNotBlank(dataDate)) {
                targetDate = sdf.parse(dataDate);
            }
            boolean result = peakPlanDeliveryService.executeDailyDataDeliveryByDate(aggregatorId, targetDate,
                    resourceTypeId);
            return ResultVO.success("调峰计划日数据上送成功");
        } catch (Exception e) {
            log.error("调峰计划申报日数据电网上送接口异常", e);
            return ResultVO.fail(StatusCode.C.getCode(), e.getMessage());
        }
    }
}
