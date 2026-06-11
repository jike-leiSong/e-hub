package cn.sl.ehub.console.model.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * 调节情况
 *
 * @author sl
 * @classes cn.sl.ehub.upstream.model.resp.HistoryAdjustExcelResp
 * @date 2026-05-28
 */
@Data
@ApiModel("调节情况")
public class HistoryAdjustExcelResp {

    List<ExcelExportEntity> entityList;


    List<Map<String, String>> allExcelDataList;
}
