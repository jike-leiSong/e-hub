package cn.sl.ehub.service.service;

import cn.sl.ehub.service.vo.ControlIssueConfig;

/**
 * 控制下发配置Service
 * @author sl
 * @date 2026-06-04
 */
public interface ControlIssueConfigService {

    ControlIssueConfig getAggregatorEnt(String remoteId);
}
