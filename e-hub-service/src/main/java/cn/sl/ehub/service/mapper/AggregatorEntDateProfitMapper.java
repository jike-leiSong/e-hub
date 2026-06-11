package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.resp.AggregatorEntDateProfitResp;
import cn.sl.ehub.service.vo.AggregatorEntDateProfit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

@Repository
public interface AggregatorEntDateProfitMapper extends Mapper<AggregatorEntDateProfit> {

    List<AggregatorEntDateProfitResp> selectRespListByParam(@Param("aggregatorId") String aggregatorId,
                                                            @Param("date") String date,
                                                            @Param("entIds") List<String> entIds);

    List<Date> selectDateListByEntIds(@Param("aggregatorId") String aggregatorId,
                                      @Param("entIds") List<String> entIds);

    /**
     * 批量添加数据
     *
     * @param aggregatorEntDateProfitList
     * @return
     */
    int batchInsert(List<AggregatorEntDateProfit> aggregatorEntDateProfitList);

    /**
     * 查询总收益
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    Double getTotalProfit(@Param("entId") String entId,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    AggregatorEntDateProfitResp getProfit(@Param("entId") String entId);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    AggregatorEntDateProfit getAggregatorEntDateProfit(@Param("entId") String entId, @Param("date") String date);
}