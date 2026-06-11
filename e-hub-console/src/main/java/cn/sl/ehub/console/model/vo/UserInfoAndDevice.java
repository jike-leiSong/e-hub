package cn.sl.ehub.console.model.vo;

import cn.sl.ehub.service.vo.AggregatorEntDevice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel("用户多层级设备关系")
public class UserInfoAndDevice {
    @ApiModelProperty("用户id")
    private String entId;
    @ApiModelProperty("用户systemCode")
    private String deviceBaseId;
    @ApiModelProperty("用户名")
    private String deviceName;
    @ApiModelProperty("设备类型,0能源站，1设备,3用户")
    private String deviceType;
    @ApiModelProperty("是否申报，0未申报,1已申报")
    private String applyStatus;
    @ApiModelProperty("是否申报，true中标,false未中标")
    private Boolean winStatu;
    @ApiModelProperty("用户设备信息")
    private List<EnergyStationInfoAndDevice> children;
}
