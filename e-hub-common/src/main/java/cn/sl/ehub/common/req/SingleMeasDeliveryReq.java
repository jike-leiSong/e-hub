package cn.sl.ehub.common.req;

import cn.sl.ehub.common.enums.EnergyModelEnum;
import cn.sl.ehub.common.enums.EnergyModelEnumNew;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description: 单体模型数据上送请求参数
 * @Author sl
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "单体测量数据上送请求参数")
public class SingleMeasDeliveryReq {

    @ApiModelProperty(value = "能源模型枚举类")
    private EnergyModelEnumNew energyModelEnumNew;

    @ApiModelProperty(value = "单体测量数据")
    private List<Object> singleMeasData;
    @ApiModelProperty(value = "单体测量设备数据")
    private List<Object> singleMeasDataDevice;

}
