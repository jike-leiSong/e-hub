package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.enums.ResourceTypeEnum;
import cn.sl.ehub.console.model.req.PeakPlanDeclareImportReq;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.console.service.IPeakPlanDeclareService;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 峰值计划申报
 *
 * @Author sl
 * @Date 2026-06-23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/peakPlanDeclare")
@Api(tags = "峰值计划申报")
@Validated
public class PeakPlanDeclareController {

    private final IPeakPlanDeclareService peakPlanDeclareService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;

    @ApiOperation(value = "导入预测数据")
    @PostMapping("/import")
    public ResultVO<String> importPredictionData(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = "开始日期不能为空") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull(message = "结束日期不能为空") Date endDate,
            @RequestParam("file") @NotNull(message = "Excel文件不能为空") MultipartFile file,
            @RequestParam("resourceType") @NotNull(message = "资源类型不能为空") String resourceType,
            @RequestParam(value = "aggregatorId", required = false) String aggregatorId) {
        try {
            log.info("接收到预测数据导入请求，startDate={}, endDate={}, resourceType={}, aggregatorId={}",
                    startDate, endDate, resourceType, aggregatorId);
            Integer resourceTypeCode = resolveResourceTypeCode(resourceType, aggregatorId);
            if (!ResourceTypeEnum.isValid(resourceTypeCode)) {
                return ResultVO.fail(500, "资源类型不支持：" + resourceType + "，当前支持的资源类型为：电采暖(15)、工业负荷(44)");
            }

            PeakPlanDeclareImportReq req = new PeakPlanDeclareImportReq();
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            req.setFile(file);
            req.setResourceType(resourceTypeCode);
            req.setAggregatorId(aggregatorId);

            String result = peakPlanDeclareService.importPeakPlanDeclare(req);
            if ("success".equals(result)) {
                return ResultVO.success("导入成功");
            }
            return ResultVO.fail(500, result);
        } catch (Exception e) {
            log.error("导入预测数据异常", e);
            return ResultVO.fail(500, "导入失败：" + e.getMessage());
        }
    }

    private Integer resolveResourceTypeCode(String resourceType, String aggregatorId) {
        String resourceTypeValue = StringUtils.trimToEmpty(resourceType);
        Integer directCode = parseResourceTypeCode(resourceTypeValue);
        if (ResourceTypeEnum.isValid(directCode)) {
            return directCode;
        }

        Integer codeByName = matchResourceTypeCode(resourceTypeValue);
        if (ResourceTypeEnum.isValid(codeByName)) {
            return codeByName;
        }

        AggregatorResourceType aggregatorResourceType = findAggregatorResourceType(resourceTypeValue, aggregatorId);
        if (aggregatorResourceType == null) {
            return null;
        }
        return matchResourceTypeCode(aggregatorResourceType.getName());
    }

    private Integer parseResourceTypeCode(String resourceType) {
        if (StringUtils.isBlank(resourceType)) {
            return null;
        }
        try {
            return Integer.valueOf(resourceType);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private AggregatorResourceType findAggregatorResourceType(String resourceTypeId, String aggregatorId) {
        if (StringUtils.isBlank(resourceTypeId)) {
            return null;
        }
        if (StringUtils.isNotBlank(aggregatorId)) {
            List<AggregatorResourceType> resourceTypeList =
                    aggregatorResourceTypeService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);
            if (resourceTypeList != null) {
                for (AggregatorResourceType item : resourceTypeList) {
                    if (item != null && StringUtils.equals(resourceTypeId, item.getId())) {
                        return item;
                    }
                }
            }
        }
        return aggregatorResourceTypeService.getTypeById(resourceTypeId);
    }

    private Integer matchResourceTypeCode(String resourceTypeName) {
        if (StringUtils.isBlank(resourceTypeName)) {
            return null;
        }
        for (ResourceTypeEnum type : ResourceTypeEnum.values()) {
            if (StringUtils.equals(resourceTypeName, String.valueOf(type.getCode()))
                    || StringUtils.equals(resourceTypeName, type.getName())
                    || StringUtils.contains(resourceTypeName, type.getName())) {
                return type.getCode();
            }
        }
        return null;
    }
}
