package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.BigScreenEntTodayRate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 全局概况Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface BigScreenEntTodayRateMapper extends Mapper<BigScreenEntTodayRate> {

    /**
     * 添加数据
     *
     * @param bigScreenEntTodayRateList
     * @return
     */
    int batchInsert(List<BigScreenEntTodayRate> bigScreenEntTodayRateList);

    /**
     * 查询数据
     *
     * @param time
     * @return
     */
    List<BigScreenEntTodayRate> getBigScreenEntTodayRateList(@Param("time") String time);
}
