package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorSms;

import java.util.List;

/**
 * 聚合商短信Service
 * @author sl
 * @date 2026-06-04
 */
public interface AggregatorSmsService {

    /**
     * 获取短信联系人列表
     * @return 短信联系人列表
     */
    List<AggregatorSms> getAggregatorSms();

    /**
     * 获取接收人列表
     * @param roleType 角色类型
     * @param stateGrid 电网类型
     * @return 接收人列表
     */
    List<AggregatorSms> getReceivers(String roleType, String stateGrid);
}
