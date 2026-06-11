# Console项目代码清理方案

## 📋 保留的Controller（8个）

根据指定的接口路径，需要保留以下Controller：

1. **YesterdayController** - `/yesterday/*`
   - `/yesterday/getResourceTypeList`
   - `/yesterday/getLastProfit`
   - `/yesterday/getOverview`

2. **TomorrowController** - `/tomorrow/*`
   - `/tomorrow/getAggregatorApply`
   - `/tomorrow/getPriceByResourceTypeId`

3. **ProfitController** - `/profit/*`
   - `/profit/week`
   - `/profit/getContentList`
   - `/profit/list`
   - `/profit/listByEntIdListExcel`

4. **EntUserDetailController** - `/entUserDetail/*`
   - `/entUserDetail/getEntUserDetailRespList`
   - `/entUserDetail/options`
   - `/entUserDetail/listV2`

5. **HistoryQueryController** - `/historyQuery/*`
   - `/historyQuery/getProfitCalculation`
   - `/historyQuery/getProfitCalculationExcel`
   - `/historyQuery/getTotalPowerChart`
   - `/historyQuery/userAdjustmentGraphNew`
   - `/historyQuery/exportAdjust`
   - `/historyQuery/exportBuZhaoUploadData`
   - `/historyQuery/profitStatistics`
   - `/historyQuery/userProfitStatistics`
   - `/historyQuery/getPrice`
   - `/historyQuery/getPriceTable`
   - `/historyQuery/getPriceExcel`

6. **TodayController** - `/today/*`
   - `/today/get/device/tree`
   - `/today/getMultiDeviceChartResp`

7. **AggregatorApplyPlanController** - `/aggregatorPlan/*`
   - `/aggregatorPlan/getRunPlan`
   - `/aggregatorPlan/getPlanList`

8. **WeatherController** - `/weather/*`
   - `/weather/getDayWeather`

9. **HealthController** - 健康检查（保留）

---

## 🗑️ 删除的Controller（约37个）

以下Controller将被删除：

1. AggregatorEntAppApplyPlanController
2. AggregatorEntApplyPlanController
3. AggregatorEntController
4. AggregatorRealTimeDateController
5. AppController
6. ApplyPlanController
7. BigScreenController
8. BigScreenInsertController
9. CacheQueryController
10. CommonSqlController
11. DataController
12. DataSupportController
13. ExternalDataController
14. FileController
15. GuangzhouAccountController
16. GuangzhouAggregatorApplyPlanController
17. GuangzhouAggregatorAreaAndEntController
18. GuangzhouAggregatorEntAppApplyPlanController
19. GuangzhouAggregatorEntChangePowerRankingController
20. GuangzhouAggregatorInviteController
21. GuangzhouDataSupportController
22. GuangzhouDataTimerController
23. GuangzhouDeviceController
24. GuangzhouHistoryInvitationController
25. GuangzhouPowerAdjustmentController
26. GuangzhouProfitController
27. GuangzhouTodayInviteController
28. GuangzhouUserManagementController
29. GuangzhouWarningInfoController
30. Main.java
31. PeakPlanDeclareController
32. PeakShavingAuxiliaryController
33. ProfitBillController
34. RedisController
35. SmsController
36. TripartDataSynchronController
37. WebSocketController

---

## 📋 保留的Job（1个）

- **AggregatorApplyPlanJob** - 聚合商申报计划Job

---

## 🗑️ 删除的Job（5个）

1. AddApplyPlanJob
2. AggregatorAutoApplyPlanJob
3. BigScreenJob
4. DealDevicePowerAndQuantityJob
5. EntAutoApplyPlanJob

---

## 📊 清理统计

| 类型 | 保留 | 删除 | 清理率 |
|------|------|------|--------|
| Controller | 9个 | 37个 | 80% |
| Job | 1个 | 5个 | 83% |

---

## ✅ 确认清理

请确认是否执行清理操作？

- [ ] 确认删除37个Controller
- [ ] 确认删除5个Job
- [ ] 确认保留9个Controller和1个Job
