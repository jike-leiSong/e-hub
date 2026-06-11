package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDateApplyDetailOffer;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 聚合商申报详情Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDateApplyDetailOfferMapper extends Mapper<AggregatorDateApplyDetailOffer> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDateApplyDetailOfferList
     * @return
     */
    int batchInsert(List<AggregatorDateApplyDetailOffer> aggregatorDateApplyDetailOfferList);
}
