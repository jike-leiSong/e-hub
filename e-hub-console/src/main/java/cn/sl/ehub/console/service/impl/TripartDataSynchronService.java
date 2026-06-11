package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.TripartDataSynchronLogMapper;
import cn.sl.ehub.console.service.ITripartDataSynchronService;
import cn.sl.ehub.console.service.TripartTanShanService;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.TripartDataSynchronLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TripartDataSynchronService implements ITripartDataSynchronService {

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:00";

    @Resource
    private TripartDataSynchronLogMapper tripartDataSynchronLogMapper;

    @Resource
    private TripartTanShanService tripartTanShanService;

    @Override
    public TripartDataSynchronLog getLogByStatus(String status) {
        Example example = new Example(TripartDataSynchronLog.class);
        example.setOrderByClause("id desc");
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("statue", status);
        RowBounds rowBounds = new RowBounds(0, 1);
        List<TripartDataSynchronLog> result = tripartDataSynchronLogMapper.selectByExampleAndRowBounds(example, rowBounds);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.get(0);
        }

        return null;
    }

    @Override
    public TripartDataSynchronLog getLogByCreateTime(String createTime) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime = DateTime.parse(createTime, dateTimeFormatter);
        String beginTime = dateTime.toString(DATE_TIME_FORMAT);
        String endTime = dateTime.plusMinutes(1).toString(DATE_TIME_FORMAT);

        Example example = new Example(TripartDataSynchronLog.class);
        example.setOrderByClause("id desc");
        Example.Criteria criteria = example.createCriteria();
        criteria.andGreaterThanOrEqualTo("createTime", beginTime);
        criteria.andLessThanOrEqualTo("createTime", endTime);
        //criteria.andEqualTo("statue", '0');
        RowBounds rowBounds = new RowBounds(0, 1);
        List<TripartDataSynchronLog> tripartDataSynchronLogs = tripartDataSynchronLogMapper.selectByExampleAndRowBounds(example, rowBounds);
        if (CollectionUtils.isNotEmpty(tripartDataSynchronLogs)) {
            return tripartDataSynchronLogs.get(0);
        }
        return null;
    }

    @Override
    public int updateLogById(TripartDataSynchronLog log) {

        return tripartDataSynchronLogMapper.updateByPrimaryKeySelective(log);

    }

    @Override
    public void addLog(TripartDataSynchronLog log) {
        executor.execute(() -> {
            tripartDataSynchronLogMapper.insert(log);
        });
    }

    @Override
    public ResultVO<String> synchronRetry(String createTime) {
        TripartDataSynchronLog tripartDataSynchronLog = new TripartDataSynchronLog();
        // 入参为空时，取最后一条失败数据
        if (StringUtils.isBlank(createTime)) {
            tripartDataSynchronLog = getLogByStatus("0");
        } else {
            // 根据时间查失败的数据
            tripartDataSynchronLog = getLogByCreateTime(createTime);
        }
        // 不为空，则表明执行失败
        if (null != tripartDataSynchronLog) {
            return tripartTanShanService.dataSynchronizeRetry(tripartDataSynchronLog);
        } else {
            // 无执行日志，直接补招
            return tripartTanShanService.dataSynchronizeRetryWithoutLog(createTime);
        }

    }
}
