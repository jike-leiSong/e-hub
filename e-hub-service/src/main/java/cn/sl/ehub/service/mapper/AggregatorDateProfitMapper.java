package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.resp.AggregatorDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorProfitResp;
import cn.sl.ehub.service.vo.AggregatorDateProfit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

@Repository
public interface AggregatorDateProfitMapper extends Mapper<AggregatorDateProfit> {

    List<AggregatorDateProfitResp> selectDateProfitWithEntDetail(@Param("aggregatorId") String aggregatorId,
                                                                 @Param("startDate") Date startDate,
                                                                 @Param("endDate") Date endDate,
                                                                 @Param("entIds") List<String> entIds);

    /**
     * 批量添加数据
     *
     * @param aggregatorDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorDateProfit> aggregatorDateProfitList);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param startDate
     * @param endDate
     * @return
     */
    AggregatorProfitResp getAggregatorProfitRespTotal(@Param("aggregatorId") String aggregatorId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}