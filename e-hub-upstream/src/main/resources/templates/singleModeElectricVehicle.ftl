<!company=${company}!>
<静态信息::电动汽车充电站>
@序号,站名,所在区域,全站容量,全站桩数量,运营系统内部站ID,站类型,是否可控
<#list electricVehicleStationList as staionidDetail>
#${staionidDetail_index+1},${staionidDetail.stationName},${staionidDetail.area},${staionidDetail.totalCapacity},${staionidDetail.chargingEquipNo},${staionidDetail.innerStationId},${staionidDetail.stationType},${staionidDetail.controllable}
</#list>
</静态信息::电动汽车充电站>
<静态信息::充电桩>
@序号,桩名,所属站,桩容量,桩类型,桩厂家,投资方,运营系统内部设备ID
<#list electricVehicleEquipList as equipDetail>
#${equipDetail_index+1},${equipDetail.equipName},${equipDetail.stationName},${equipDetail.equipCapacity},${equipDetail.equipType},${equipDetail.equipManufactor},${equipDetail.investor},${equipDetail.innerEquipId}
</#list>
</静态信息::充电桩>
