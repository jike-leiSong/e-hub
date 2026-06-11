package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * 聚合商信息Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorInfoMapper extends Mapper<AggregatorInfo> {

    /**
     * 查询第一条数据
     *
     * @return
     */
    AggregatorInfo getFirst();
}
