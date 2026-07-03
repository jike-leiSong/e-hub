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

    @ApiModelProperty("导入批次号")
    private String importBatchNo;

    @ApiModelProperty("发布时间")
    private String publishTime;
}
