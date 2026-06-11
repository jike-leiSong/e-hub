package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.ClearIssueLogMapper;
import cn.sl.ehub.service.vo.ClearIssueLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class ClearIssueLogService {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    @Resource
    private ClearIssueLogMapper clearIssueLogMapper;

    public void addLog(ClearIssueLog log) {
        clearIssueLogMapper.insertSelective(log);
    }

    public ClearIssueLog getLastedLog() {
        Example example = new Example(ClearIssueLog.class);
        example.setOrderByClause("create_time desc");
        List<ClearIssueLog> clearIssueLogs = clearIssueLogMapper.selectByExampleAndRowBounds(example, new RowBounds(0, 1));
        if (CollectionUtils.isEmpty(clearIssueLogs)) {
            return new ClearIssueLog();
        } else {
            return clearIssueLogs.get(0);
        }
    }

    public String getTodayLastedCmdData() {
        Example example = new Example(ClearIssueLog.class);
        example.setOrderByClause("create_time desc");
//        PageHelper.startPage(1, 1);
        List<ClearIssueLog> clearIssueLogs = clearIssueLogMapper.selectByExampleAndRowBounds(example, new RowBounds(0, 1));
        if (CollectionUtils.isEmpty(clearIssueLogs)) {
            return "";
        } else {
            ClearIssueLog clearIssueLog = clearIssueLogs.get(0);
            return processTodayLastedCmdData(clearIssueLog);
        }
    }

    private String processTodayLastedCmdData(ClearIssueLog clearIssueLog) {
        DateTime now = DateTime.now();
        String date = now.toString(YYYY_MM_DD);
        DateTime createTime = new DateTime(clearIssueLog.getCreateTime());
        String createDate = createTime.toString(YYYY_MM_DD);
        if (StringUtils.equals(date, createDate)) {
            return clearIssueLog.getCmdData();
        } else return "";
    }
}
