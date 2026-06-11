package cn.sl.ehub.console.model.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 设备收益数据对照表
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@ApiModel("设备收益数据对照表")
@Data
public class AggregatorDeviceDateProfitExportExcelResp {

    @Excel(name = "企业用户ID")
    private String entId;
    @Excel(name = "企业用户名称")
    private String entName;
    @Excel(name = "资源类型ID")
    private String resourceTypeId;
    @Excel(name = "资源类型名称")
    private String resourceTypeName;
    @Excel(name = "设备ID")
    private String deviceBaseId;
    @Excel(name = "设备名称")
    private String deviceName;
    @Excel(name = "日期")
    private String date;
    @Excel(name = "开始时间")
    private String startTime;
    @Excel(name = "结束时间")
    private String endTime;
    @Excel(name = "申报调节功率")
    private Double deliveryPower;
    @Excel(name = "实际调节功率")
    private Double reallyPower;
    @Excel(name = "下发调节功率")
    private Double issuePower;
    @Excel(name = "最小功率")
    private Double minPower;
    @Excel(name = "基线负荷")
    private Double baseLinePower;
    @Excel(name = "计算负荷")
    private Double countPower;
    @Excel(name = "预计调节功率")
    private Double estimatePower;
    @Excel(name = "预计调节用电量")
    private Double estimateElectricQuantity;
    @Excel(name = "功率占比")
    private Double powerPercent;
    @Excel(name = "用电量")
    private Double electricQuantity;
    @Excel(name = "收益")
    private Double profit;
    @Excel(name = "出清价格")
    private Double countPrice;

    /**
     * 转换数据
     *
     * @param aggregatorDeviceDateProfitResp
     * @return
     */
    public static AggregatorDeviceDateProfitExportExcelResp trans(AggregatorDeviceDateProfitResp aggregatorDeviceDateProfitResp) {
        AggregatorDeviceDateProfitExportExcelResp aggregatorDeviceDateProfitExportExcelResp = new AggregatorDeviceDateProfitExportExcelResp();
        if (null != aggregatorDeviceDateProfitResp) {
            BeanUtils.copyProperties(aggregatorDeviceDateProfitResp, aggregatorDeviceDateProfitExportExcelResp);
        }
        return aggregatorDeviceDateProfitExportExcelResp;
    }

    /**
     * 转换数据
     *
     * @param aggregatorDeviceDateProfitRespList
     * @return
     */
    public static List<AggregatorDeviceDateProfitExportExcelResp> transList(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList) {
        List<AggregatorDeviceDateProfitExportExcelResp> aggregatorDeviceDateProfitExportExcelRespList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespList)) {
            aggregatorDeviceDateProfitRespList.forEach(aggregatorDeviceDateProfitResp -> {
                aggregatorDeviceDateProfitExportExcelRespList.add(trans(aggregatorDeviceDateProfitResp));
            });
        }
        return aggregatorDeviceDateProfitExportExcelRespList;
    }
}
