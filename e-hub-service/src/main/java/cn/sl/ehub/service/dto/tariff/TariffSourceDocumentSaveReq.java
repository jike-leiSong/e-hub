package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电价来源文档保存请求")
public class TariffSourceDocumentSaveReq {

    @ApiModelProperty("主键ID，更新时必填")
    private Long id;

    @ApiModelProperty("来源配置ID")
    private Long sourceConfigId;

    @ApiModelProperty("导入批次号")
    private String batchNo;

    @ApiModelProperty("电价月份 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("内部版本")
    private String version;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("来源地址")
    private String sourceUrl;

    @ApiModelProperty("来源文件名")
    private String sourceFileName;

    @ApiModelProperty("来源文件路径")
    private String sourceFilePath;

    @ApiModelProperty("来源文件hash")
    private String sourceFileHash;

    @ApiModelProperty("文档标题")
    private String documentTitle;

    @ApiModelProperty("文号")
    private String documentNo;

    @ApiModelProperty("发布时间 yyyy-MM-dd HH:mm:ss")
    private String publishTime;

    @ApiModelProperty("生效开始 yyyy-MM-dd")
    private String effectiveStart;

    @ApiModelProperty("生效结束 yyyy-MM-dd")
    private String effectiveEnd;

    @ApiModelProperty("状态 DRAFT/PUBLISHED/ARCHIVED")
    private String status;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("操作人名称")
    private String operatorName;

    @ApiModelProperty("备注")
    private String remark;
}
