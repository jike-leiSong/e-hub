package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.req.EntMonthElectricProfitReq;
import cn.sl.ehub.service.resp.EntMonthElectricProfitResp;
import cn.sl.ehub.console.service.IAggregatorEntDateProfitService;
import cn.sl.ehub.console.service.IAggregatorEntService;
import cn.sl.ehub.console.service.IExternalDataService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 外部调用接口ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class ExternalDataServiceImpl implements IExternalDataService {

    private final IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    private final IAggregatorEntService aggregatorEntService;

    @Override
    public List<EntMonthElectricProfitResp> getEntDataRespList(EntMonthElectricProfitReq req) {
        List<EntMonthElectricProfitResp> respList = Lists.newArrayList();
        //查询企业信息
        Map<String, String> entInfoMap = new HashMap<>();
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(req.getEntIdList());
        if (CollectionUtils.isNotEmpty(aggregatorEntList)) {
            Map<String, String> queryEntMap = aggregatorEntList.stream().collect(Collectors.toMap(AggregatorEnt::getEntId, AggregatorEnt::getEntName, (v1, v2) -> v1));
            if (null != queryEntMap && queryEntMap.size() > 0) {
                entInfoMap.putAll(queryEntMap);
            }
        }
        //查询收益数据
        List<AggregatorEntDateProfit> queryList = aggregatorEntDateProfitService.getAggregatorEntDateProfitList(req.getEntIdList(), req.getMonth() + "-01", req.getMonth() + "-31");
        if (CollectionUtils.isNotEmpty(queryList)) {
            Map<String, List<AggregatorEntDateProfit>> entMap = queryList.stream().collect(groupingBy(AggregatorEntDateProfit::getEntId));
            req.getEntIdList().forEach(entId -> {
                EntMonthElectricProfitResp resp = new EntMonthElectricProfitResp();
                resp.setEntId(entId);
                resp.setEntName(entInfoMap.get(entId));
                resp.setMonth(req.getMonth());
                List<AggregatorEntDateProfit> profitList = entMap.get(entId);
                if (CollectionUtils.isNotEmpty(profitList)) {
                    resp.setElectric(0D);
                    double electric = profitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).mapToDouble(AggregatorEntDateProfit::getElectricQuantity).sum();
                    resp.setElectric(MathUtils.doublePoint(resp.getElectric() + electric, 2));
                    resp.setProfit(0D);
                    double entProfit = profitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).mapToDouble(AggregatorEntDateProfit::getEntProfit).sum();
                    resp.setProfit(MathUtils.doublePoint(resp.getProfit() + entProfit, 2));
                }
                if (StringUtils.isNotEmpty(resp.getEntName())) {
                    respList.add(resp);
                }
            });
        }
        return respList;
    }
}
