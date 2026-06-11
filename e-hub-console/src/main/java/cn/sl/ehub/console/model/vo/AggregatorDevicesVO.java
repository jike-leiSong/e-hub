package cn.sl.ehub.console.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel("聚合商多层级设备关系")
public class AggregatorDevicesVO {


    @ApiModelProperty("用户名")
    private List<UserInfoAndDevice> UserInfoAndDevice;
}
