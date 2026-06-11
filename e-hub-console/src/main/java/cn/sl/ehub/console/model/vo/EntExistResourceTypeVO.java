package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.common.vo.DataResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel(value = "用户资源id详情")
public class EntExistResourceTypeVO {
    @ApiModelProperty(value = "实际功率曲线")
    private String entId;
    @ApiModelProperty("主键ID")
    private String resourceTypeId;
    @ApiModelProperty("名称")
    private String resourceTypeName;
}
