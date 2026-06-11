<!company=${company}!>
<静态信息::工业负荷>
@序号,用户名,容量,所在区域,用户类型,业主方,运营系统内部用户ID,是否参与
<#list detailList as detail>
#${detail_index+1},${detail.username},${detail.capacity},${detail.area},${detail.userType},${detail.owner},${detail.innerStationId},${detail.participation}
</#list>
</静态信息::工业负荷>
