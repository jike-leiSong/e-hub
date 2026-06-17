package cn.sl.ehub.console.service.impl;/**
 * @ProjectName: load-aggregator
 * @Package: cn.sl.ehub.upstream.service.impl
 * @ClassName: AggregatorSmsServiceImpl
 * @Author sl
 * @Description: 聚合商用户联系方式
 * @Date 2026-05-28
 * @Version: 1.0
 */

import cn.sl.ehub.service.req.UpdateEntPhoneReq;
import cn.sl.ehub.console.service.IAggregatorSmsService;
import cn.sl.ehub.service.mapper.AggregatorSmsMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorSms;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sl
 * @date 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class AggregatorSmsServiceImpl implements IAggregatorSmsService {

    private final AggregatorSmsMapper aggregatorSmsMapper;

    @Override
    public List<AggregatorSms> getAggregatorSms() {
        Weekend<AggregatorSms> weekend = Weekend.of(AggregatorSms.class);
        WeekendCriteria<AggregatorSms, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorSms::getQueryFlag, 1);
        return aggregatorSmsMapper.selectByExample(weekend);
    }

    @Override
    public List<AggregatorSms> getEntWarningSend(String entId) {
        Weekend<AggregatorSms> weekend = Weekend.of(AggregatorSms.class);
        WeekendCriteria<AggregatorSms, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorSms::getEntId, entId);
        criteria.andEqualTo(AggregatorSms::getQueryFlag, 1);
        return aggregatorSmsMapper.selectByExample(weekend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(List<UpdateEntPhoneReq> phoneList, String entId) {
        List<AggregatorSms> addList = Lists.newArrayList();
        List<AggregatorSms> oldSmsList = getEntWarningSend(entId);
        if (CollectionUtils.isNotEmpty(phoneList)) {
            if (CollectionUtils.isEmpty(oldSmsList)) {
                //如果第一次录入直接入库
                phoneList.forEach(phone -> {
                    AggregatorSms sms = new AggregatorSms();
                    sms.setName(phone.getSmsName());
                    sms.setPhone(phone.getSmsPhone());
                    sms.setRole("0");
                    sms.setEntId(entId);
                    sms.setQueryFlag(1);
                    sms.setGridCode("HUABEI");
                    sms.setAcceptable(0);
                    addList.add(sms);
                });
            } else {
                Map<String, AggregatorSms> phoneMap = oldSmsList.stream().collect(Collectors.toMap(AggregatorSms::getPhone, Function.identity(), (v1, v2) -> v1));
                phoneList.forEach(phone -> {
                    AggregatorSms aggregatorSms = phoneMap.get(phone.getSmsPhone());
                    if (null != aggregatorSms) {
                        aggregatorSms.setName(phone.getSmsName());
                    } else {
                        aggregatorSms = new AggregatorSms();
                        aggregatorSms.setName(phone.getSmsName());
                        aggregatorSms.setPhone(phone.getSmsPhone());
                        aggregatorSms.setRole("0");
                        aggregatorSms.setEntId(entId);
                        aggregatorSms.setQueryFlag(1);
                        aggregatorSms.setGridCode("HUABEI");
                        aggregatorSms.setAcceptable(0);
                    }
                    addList.add(aggregatorSms);
                });
            }
        }
        Weekend<AggregatorEntDevice> weekend = Weekend.of(AggregatorEntDevice.class);
        WeekendCriteria<AggregatorEntDevice, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorEntDevice::getEntId, entId);
        aggregatorSmsMapper.deleteByExample(weekend);
        if(CollectionUtils.isNotEmpty(addList)) {
            aggregatorSmsMapper.batchInsert(addList);
        }
        return phoneList.size();
    }

}
