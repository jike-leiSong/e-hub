package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.BigScreenGeneralSituation;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * 全局概况Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface BigScreenGeneralSituationMapper extends Mapper<BigScreenGeneralSituation> {

    /**
     * 查询数据
     *
     * @return
     */
    BigScreenGeneralSituation getBigScreenGeneralSituation();
}
