package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价来源文档查询请求")
public class TariffSourceDocumentQueryReq {

    @ApiModelProperty("电价月份 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("内部版本")
    private String version;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("状态 DRAFT/PUBLISHED/ARCHIVED")
    private String status;

    @ApiModelProperty("导入批次号")
    private String batchNo;

    @ApiModelProperty("来源名称")
    private String sourceName;
}
