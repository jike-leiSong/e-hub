package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.service.vo.AggregatorEntDevice;
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
@ApiModel("能源站多层级设备关系")
public class EnergyStationInfoAndDevice {

    @ApiModelProperty("能源站Code")
    private String deviceBaseId;
    @ApiModelProperty("能源站名")
    private String deviceName;
    @ApiModelProperty("资源类型")
    private String resourceTypeId;
    @ApiModelProperty("设备类型,0能源站，1设备")
    private String deviceType;
    @ApiModelProperty("设备信息")
    private List<AggregatorEntDevice> children;
}
