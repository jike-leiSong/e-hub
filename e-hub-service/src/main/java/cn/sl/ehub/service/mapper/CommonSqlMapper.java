package cn.sl.ehub.service.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 公共SQLMapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface CommonSqlMapper {

    /**
     * 修改数据库
     *
     * @param sql
     */
    void updateDataBase(@Param("sql") String sql);
}
