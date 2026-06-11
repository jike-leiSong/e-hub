<!company=${company}!>
<静态信息::电动汽车充电站>
@序号,站名,全站总有功, 全站参与调节总有功,全站当日零点电量,运营系统内部站ID
<#list electricVehicleStationList as staionidDetail>
#${staionidDetail_index+1},${staionidDetail.stationName},${staionidDetail.totalPower},${staionidDetail.regularTotalPower},${staionidDetail.todayZeroElecQuantity},${staionidDetail.innerStationId}
</#list>
</静态信息::电动汽车充电站>
<静态信息::充电桩>
@序号,桩名,所属站,桩有功,桩电流,桩当日零点电量,运营系统内部设备ID
<#list electricVehicleEquipList as equipDetail>
#${equipDetail_index+1},${equipDetail.equipName},${equipDetail.stationName},${equipDetail.equipPower},${equipDetail.equipElecCurrent},${equipDetail.equipzeroElecQuanlity},${equipDetail.innerEquipId}
</#list>
</静态信息::充电桩>
