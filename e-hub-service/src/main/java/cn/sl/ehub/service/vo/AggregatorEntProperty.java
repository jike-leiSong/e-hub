package cn.sl.ehub.service.vo;

import javax.persistence.*;

@Table(name = "aggregator_ent_property")
public class AggregatorEntProperty {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "ent_id")
    private String entId;

    @Column(name = "resource_type_id")
    private String resourceTypeId;

    /**
     * 容量
     */
    @Column(name = "capacity")
    private String capacity;

    /**
     * 容量单位
     */
    @Column(name = "unit")
    private String unit;

    /**
     * 行政编码
     */
    @Column(name = "area_code")
    private String areaCode;

    /**
     * 用户类型
     */
    @Column(name = "user_type")
    private String userType;

    /**
     * 制造商
     */
    @Column(name = "equip_manufactor")
    private String equipManufactor;

    /**
     * 存储类型
     */
    @Column(name = "storage_type")
    private String storageType;

    /**
     * 所有者
     */
    @Column(name = "owner")
    private String owner;

    /**
     * 是否可控
     */
    @Column(name = "controllable")
    private String controllable;

    /**
     * 状态标识
     */
    @Column(name = "status")
    private String status;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return station_id
     */
    public String getStationId() {
        return stationId;
    }

    /**
     * @param stationId
     */
    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    /**
     * @return ent_id
     */
    public String getEntId() {
        return entId;
    }

    /**
     * @param entId
     */
    public void setEntId(String entId) {
        this.entId = entId;
    }

    /**
     * @return resource_type_id
     */
    public String getResourceTypeId() {
        return resourceTypeId;
    }

    /**
     * @param resourceTypeId
     */
    public void setResourceTypeId(String resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    /**
     * 获取容量
     *
     * @return capacity - 容量
     */
    public String getCapacity() {
        return capacity;
    }

    /**
     * 设置容量
     *
     * @param capacity 容量
     */
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    /**
     * 获取容量单位
     *
     * @return unit - 容量单位
     */
    public String getUnit() {
        return unit;
    }

    /**
     * 设置容量单位
     *
     * @param unit 容量单位
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 获取行政编码
     *
     * @return area_code - 行政编码
     */
    public String getAreaCode() {
        return areaCode;
    }

    /**
     * 设置行政编码
     *
     * @param areaCode 行政编码
     */
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    /**
     * 获取用户类型
     *
     * @return user_type - 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 获取制造商
     *
     * @return equip_manufactor - 制造商
     */
    public String getEquipManufactor() {
        return equipManufactor;
    }

    /**
     * 设置制造商
     *
     * @param equipManufactor 制造商
     */
    public void setEquipManufactor(String equipManufactor) {
        this.equipManufactor = equipManufactor;
    }

    /**
     * 获取存储类型
     *
     * @return storage_type - 存储类型
     */
    public String getStorageType() {
        return storageType;
    }

    /**
     * 设置存储类型
     *
     * @param storageType 存储类型
     */
    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * 获取所有者
     *
     * @return owner - 所有者
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 设置所有者
     *
     * @param owner 所有者
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 获取是否可控
     *
     * @return controllable - 是否可控
     */
    public String getControllable() {
        return controllable;
    }

    /**
     * 设置是否可控
     *
     * @param controllable 是否可控
     */
    public void setControllable(String controllable) {
        this.controllable = controllable;
    }

    /**
     * 获取状态标识
     *
     * @return status - 状态标识
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态标识
     *
     * @param status 状态标识
     */
    public void setStatus(String status) {
        this.status = status;
    }
}