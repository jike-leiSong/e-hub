package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorEntDeviceIotLogMapper;
import cn.sl.ehub.service.req.AggregatorEntDeviceIotLogReq;
import cn.sl.ehub.console.service.IAggregatorEntDeviceIotLogService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorEntDeviceIotLog;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备下发指令日志ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntDeviceIotLogServiceImpl implements IAggregatorEntDeviceIotLogService {

    private final AggregatorEntDeviceIotLogMapper aggregatorEntDeviceIotLogMapper;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AggregatorEntDeviceIotLog> aggregatorEntDeviceIotLogList) {
        return aggregatorEntDeviceIotLogMapper.batchInsert(aggregatorEntDeviceIotLogList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertReq(List<AggregatorEntDeviceIotLogReq> aggregatorEntDeviceIotLogReqList) {
        List<AggregatorEntDeviceIotLog> aggregatorEntDeviceIotLogList = Lists.newArrayList();
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorEntDeviceIotLogReqList.stream().map(AggregatorEntDeviceIotLogReq::getDeviceBaseId).collect(Collectors.toList()));
        Map<String, AggregatorEntDevice> deviceBaseIdMap = aggregatorEntDeviceList.stream().collect(Collectors.toMap(AggregatorEntDevice::getDeviceBaseId, Function.identity(), (v1, v2) -> v1));
        aggregatorEntDeviceIotLogReqList.forEach(aggregatorEntDeviceIotLogReq -> {
            if (StringUtils.isEmpty(aggregatorEntDeviceIotLogReq.getSendTime())) {
                aggregatorEntDeviceIotLogReq.setSendTime(DateUtils.getTime());
            }
            AggregatorEntDeviceIotLog aggregatorEntDeviceIotLog = new AggregatorEntDeviceIotLog();
            aggregatorEntDeviceIotLog.setDeviceBaseId(aggregatorEntDeviceIotLogReq.getDeviceBaseId());
            aggregatorEntDeviceIotLog.setSendTime(aggregatorEntDeviceIotLogReq.getSendTime());
            aggregatorEntDeviceIotLog.setResultMsg(aggregatorEntDeviceIotLogReq.getResultMsg());
            AggregatorEntDevice aggregatorEntDevice = deviceBaseIdMap.get(aggregatorEntDeviceIotLogReq.getDeviceBaseId());
            if (null != aggregatorEntDevice) {
                aggregatorEntDeviceIotLog.setAggregatorId(aggregatorEntDevice.getAggregatorId());
                aggregatorEntDeviceIotLog.setEntId(aggregatorEntDevice.getEntId());
                aggregatorEntDeviceIotLog.setStationId(aggregatorEntDevice.getStationId());
                aggregatorEntDeviceIotLog.setResourceTypeId(aggregatorEntDevice.getResourceTypeId());
                aggregatorEntDeviceIotLog.setDeviceId(aggregatorEntDevice.getDeviceId());
                aggregatorEntDeviceIotLog.setDeviceName(aggregatorEntDevice.getDeviceName());
                aggregatorEntDeviceIotLog.setDeviceType(aggregatorEntDevice.getDeviceType());
            }
            aggregatorEntDeviceIotLogList.add(aggregatorEntDeviceIotLog);
        });
        return aggregatorEntDeviceIotLogMapper.batchInsert(aggregatorEntDeviceIotLogList);
    }
}
