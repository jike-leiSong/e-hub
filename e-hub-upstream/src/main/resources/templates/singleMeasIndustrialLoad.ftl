<!company=${company}!>
<动态信息::工业负荷>
@序号,用户名,用户有功,用户无功,用户电流,用户当日零点电量,运营系统内部用户ID
<#list detailList as detail>
#${detail_index+1},${detail.username},${detail.userActivePower},${detail.userReactivePower},${detail.userElecCurrent},${detail.todayZeroElecQuantity},${detail.innerStationId}
</#list>
</动态信息::工业负荷>
