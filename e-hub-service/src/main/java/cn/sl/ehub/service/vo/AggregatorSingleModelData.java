package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Data
@ApiModel("单体模型表")
@Table(name = "aggregator_single_model_data")
public class AggregatorSingleModelData {
    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("能源站")
    @Column(name = "energy_station")
    private String energyStation;
    @ApiModelProperty("能源站code")
    @Column(name = "energy_station_code")
    private String energyStationCode;
    @ApiModelProperty("容量")
    @Column(name = "power_cap")
    private String powerCap;
    @ApiModelProperty("区域")
    @Column(name = "area")
    private String area;
    @ApiModelProperty("用户类型")
    @Column(name = "user_type")
    private String userType;
    @ApiModelProperty("设备制造商")
    @Column(name = "device_manufacture")
    private String deviceManufacture;
    @ApiModelProperty("设备制造商")
    @Column(name = "save_heat")
    private String saveHeat;
    @ApiModelProperty("业主方")
    @Column(name = "owner")
    private String owner;
    @ApiModelProperty("是否可控1可控")
    @Column(name = "controll")
    private String controll;
    @Transient
    @ApiModelProperty("企业ID")
    private String entId;
    @Transient
    @ApiModelProperty("企业名称")
    private String entName;
}
