package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorSms;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface AggregatorSmsMapper extends Mapper<AggregatorSms> {

    /**
     * 批量添加数据
     *
     * @param aggregatorSmsList
     * @return
     */
    int batchInsert(List<AggregatorSms> aggregatorSmsList);
}
