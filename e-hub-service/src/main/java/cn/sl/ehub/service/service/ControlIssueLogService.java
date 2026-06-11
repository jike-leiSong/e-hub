package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.ControlIssueLogMapper;
import cn.sl.ehub.service.vo.ControlIssueLog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 控制下发日志Service
 * @author sl
 * @date 2026-06-03
 */
@Service
public class ControlIssueLogService {

    @Resource
    private ControlIssueLogMapper controlIssueLogMapper;

    /**
     * 添加日志
     */
    public void addLog(ControlIssueLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(new Date());
        }
        controlIssueLogMapper.insertSelective(log);
    }

    /**
     * 根据组号获取最新日志
     */
    public ControlIssueLog getLastLogByGroupNo(String groupNo) {
        ControlIssueLog log = controlIssueLogMapper.selectLastLogByGroupNo(groupNo);
        return log != null ? log : new ControlIssueLog();
    }

    /**
     * 根据组号和时间范围获取最新日志
     */
    public ControlIssueLog getLastLogByGroupNoCustom(String groupNo, Date startTime) {
        ControlIssueLog log = controlIssueLogMapper.selectLastLogByGroupNoAndTime(groupNo, startTime);
        return log != null ? log : new ControlIssueLog();
    }

    /**
     * 根据组号和时间范围获取最新日志（String类型时间）
     */
    public ControlIssueLog getLastLogByGroupNoCustom(String groupNo, String startTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(startTime);
            return getLastLogByGroupNoCustom(groupNo, date);
        } catch (Exception e) {
            return new ControlIssueLog();
        }
    }
}

