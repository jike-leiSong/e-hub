<!company=${company}!>
<动态信息::分布式储能站>
@序号,站名,全站有功,全站无功,全站当日零点电量,运营系统内部站ID
<#list detailList as detail>
#${detail_index+1},${detail.stationName},${detail.totalActivePower},${detail.totalReactivePower},${detail.todayZeroElecQuantity},${detail.innerStationId}
</#list>
</动态信息::分布式储能站>
