<!company=${company}!>
<静态信息::电采暖>
@序号,用户名,容量,所在区域,用户类型,设备厂家,蓄热方式,业主方,是否可控,运营系统内部设备ID
<#list detailList as detail>
#${detail_index+1},${detail.username},${detail.capacity},${detail.area},${detail.userType},${detail.equipManufactor},${detail.storageType},${detail.owner},${detail.controllable},${detail.innerStationId}
</#list>
</静态信息::电采暖>