<!company=${company}!>
<静态信息::分布式储能站>
@序号,站名,所在区域,全站容量,运营系统内部站ID
<#list detailList as detail>
#${detail_index+1},${detail.stationName},${detail.area},${detail.totalCapacity},${detail.innerStationId}
</#list>
</静态信息::分布式储能站>
