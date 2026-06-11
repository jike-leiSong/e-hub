package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorEntDateAdjust;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@Repository
public interface AggregatorEntDateAdjustMapper extends Mapper<AggregatorEntDateAdjust> {
    /**
     * 批量添加数据
     *
     * @param aggregatorEntDateAdjustList
     * @return
     */
    int batchInsert(List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList);
}
