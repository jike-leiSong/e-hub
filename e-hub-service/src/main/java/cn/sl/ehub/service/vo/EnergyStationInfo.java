package cn.sl.ehub.service.vo;

/**
 * @Author sl
 * @Date 2026-05-28
 **/

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
@Data
@ApiModel("设备信息")
@Table(name = "energy_station_info")
public class EnergyStationInfo {
    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业编码")
    @Column(name = "station_id")
    private String stationId;
    @ApiModelProperty("企业名称")
    @Column(name = "ent_name")
    private String entName;
    @ApiModelProperty("能源站")
    @Column(name = "energy_station")
    private String energyStation;
    @ApiModelProperty("能源站code")
    @Column(name = "energy_station_code")
    private String energyStationCode;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("能源站类型")
    @Column(name = "energy_station_type")
    private String energyStationType;
    @ApiModelProperty("是否可控")
    @Column(name = "energy_station_controller")
    private String energyStationController;
    @ApiModelProperty("区域编码")
    @Column(name = "area_code")
    private String areaCode;
}
