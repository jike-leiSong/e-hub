package cn.sl.ehub.service.mapper;

import java.util.Date;
import java.util.List;

import cn.sl.ehub.service.vo.MosPeakThirdPartyBidData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import tk.mybatis.mapper.common.Mapper;

/**
 * 调峰市场第三方申报数据Mapper
 *
 * @author sl
 * @date 2026-05-28
 */
@Repository
public interface MosPeakThirdPartyBidDataMapper extends Mapper<MosPeakThirdPartyBidData> {

    /**
     * 批量插入或更新数据
     *
     * @param dataList 数据列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(List<MosPeakThirdPartyBidData> dataList);

    /**
     * 根据聚合商ID、资源ID和日期范围删除数据
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 影响行数
     */
    int deleteByPhyunitIdAndDateRange(@Param("aggregatorId") String aggregatorId,
            @Param("sourceId") String sourceId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * 根据聚合商ID、资源ID和日期范围查询数据
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 数据列表
     */
    List<MosPeakThirdPartyBidData> selectByPhyunitIdAndDateRange(@Param("aggregatorId") String aggregatorId,
            @Param("sourceId") String sourceId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}
