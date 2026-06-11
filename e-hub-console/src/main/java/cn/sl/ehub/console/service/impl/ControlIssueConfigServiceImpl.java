package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.ControlIssueConfigMapper;
import cn.sl.ehub.service.vo.ControlIssueConfig;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public class ControlIssueConfigServiceImpl {
    @Autowired
    ControlIssueConfigMapper controlIssueConfigMapper;
    public ControlIssueConfig getAggregatorEnt(String remoteId) {
        Weekend<ControlIssueConfig> weekend = Weekend.of(ControlIssueConfig.class);
        WeekendCriteria<ControlIssueConfig, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(ControlIssueConfig::getId, remoteId);
        List<ControlIssueConfig> aggregatorEntList = controlIssueConfigMapper.selectByExample(weekend);
        if (null != aggregatorEntList && aggregatorEntList.size() > 0) {
            return aggregatorEntList.get(0);
        }
        return null;
    }
}
