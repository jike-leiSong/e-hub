package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("IoT选项")
public class IotOptionVO {

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("展示名")
    private String label;
}
