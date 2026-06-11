package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface AggregatorDeviceDateProfitMapper extends Mapper<AggregatorDeviceDateProfit> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDeviceDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList);
}