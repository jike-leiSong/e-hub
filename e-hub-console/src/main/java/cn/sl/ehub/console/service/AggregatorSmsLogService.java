package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorSmsLog;

/**
 * 聚合商短信日志Service
 * @author sl
 * @date 2026-06-04
 */
public interface AggregatorSmsLogService {

    /**
     * 保存短信日志
     * @param log 短信日志
     */
    void save(AggregatorSmsLog log);
}
