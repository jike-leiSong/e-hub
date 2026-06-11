package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备信息
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备信息")
@Table(name = "aggregator_ent_device")
public class AggregatorEntDevice {

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
    @ApiModelProperty("能源站")
    @Column(name = "energy_station")
    private String energyStation;
    @ApiModelProperty("能源站code")
    @Column(name = "energy_station_code")
    private String energyStationCode;
    @ApiModelProperty("设备ID")
    @Column(name = "device_base_id")
    private String deviceBaseId;
    @ApiModelProperty("设备名称")
    @Column(name = "device_name")
    private String deviceName;
    @ApiModelProperty("设备类型")
    @Column(name = "device_type")
    private String deviceType;
    @ApiModelProperty("设备编码")
    @Column(name = "device_id")
    private String deviceId;
    @ApiModelProperty("户号")
    @Column(name = "account_no")
    private String accountNo;
    @ApiModelProperty("1-在线，0-下线")
    @Column(name = "status")
    private Integer status;
    @ApiModelProperty("模型上传标识")
    @Column(name = "model_flag")
    private Integer modelFlag;
    @ApiModelProperty("功率")
    @Column(name = "power")
    private Double power;
    @ApiModelProperty("最高运行负荷")
    @Column(name = "max_power")
    private Double maxPower;
    @ApiModelProperty("响应能力")
    @Column(name = "response_power")
    private Double responsePower;
    @ApiModelProperty("设备域")
    @Column(name = "data_source")
    private String dataSource;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("下发设备ID")
    @Column(name = "iot_device_base_id")
    private String iotDeviceBaseId;
    @ApiModelProperty("区域编码")
    @Column(name = "area_code")
    private String areaCode;
    @ApiModelProperty("设备用途")
    @Column(name = "user_type")
    private String userType;
    @ApiModelProperty("设备制造商")
    @Column(name = "equip_manufactor")
    private String equipManufactor;
    @ApiModelProperty("存储方式")
    @Column(name = "storage_type")
    private String storageType;
    @ApiModelProperty("是否可控")
    @Column(name = "controllable")
    private Integer controllable;
    @ApiModelProperty("企业名称")
    private String username;
    @ApiModelProperty("归属地区域编码")
    @Column(name = "state_grid_code")
    private String stateGridCode;
    @ApiModelProperty("1-在用，0-删除，2-审核中")
    @Column(name = "del_flag")
    private Integer delFlag;
    @ApiModelProperty("1-公用，0-专用")
    @Column(name = "is_public")
    private Boolean isPublic;
    @ApiModelProperty("1-公用，0-专用")
    @Column(name = "is_direct")
    private Boolean isDirect;
}
