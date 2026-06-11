package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.RetryIssueLogMapper;
import cn.sl.ehub.service.vo.RetryIssueLog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class RetryIssueLogService {

    @Resource
    private RetryIssueLogMapper retryIssueLogMapper;

    public void addLog(RetryIssueLog log){
        retryIssueLogMapper.insertSelective(log);
    }
}
