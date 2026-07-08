package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代理电价数据来源")
public class AgentPriceSourceResp {

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("来源地址")
    private String sourceUrl;

    @ApiModelProperty("来源文件名")
    private String sourceFileName;

    @ApiModelProperty("来源文件hash")
    private String sourceFileHash;

    @ApiModelProperty("导入批次号")
    private String importBatchNo;

    @ApiModelProperty("文档标题")
    private String documentTitle;

    @ApiModelProperty("文号")
    private String documentNo;

    @ApiModelProperty("发布时间")
    private String publishTime;

    @ApiModelProperty("生效开始")
    private String effectiveStart;

    @ApiModelProperty("生效结束")
    private String effectiveEnd;
}
