package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorResourceDateDeliveryOffer;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 聚合商资源类型日期申报价格Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorResourceDateDeliveryOfferMapper extends Mapper<AggregatorResourceDateDeliveryOffer> {

    /**
     * 批量添加数据
     *
     * @param aggregatorResourceDateDeliveryOfferList
     * @return
     */
    int batchInsert(List<AggregatorResourceDateDeliveryOffer> aggregatorResourceDateDeliveryOfferList);
}
