package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorEntApplyDateCheck;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 企业申报计划日期校验Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntApplyDateCheckMapper extends Mapper<AggregatorEntApplyDateCheck> {

    /**
     * 批量添加数据
     *
     * @param aggregatorEntApplyDateCheckList
     * @return
     */
    int batchInsert(List<AggregatorEntApplyDateCheck> aggregatorEntApplyDateCheckList);
}
