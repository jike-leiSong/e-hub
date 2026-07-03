package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("字典项响应")
public class DictItemResp {

    @ApiModelProperty("字典类型")
    private String dictType;

    @ApiModelProperty("编码")
    private String itemCode;

    @ApiModelProperty("名称")
    private String itemName;

    @ApiModelProperty("值")
    private String itemValue;

    @ApiModelProperty("排序")
    private Integer sortNo;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("扩展JSON")
    private String extJson;
}
