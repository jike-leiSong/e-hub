package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleOperationLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleOperationLogMapper extends Mapper<ConsoleOperationLog> {

    List<ConsoleOperationLog> page(@Param("bizType") String bizType,
                                   @Param("operatorUserId") String operatorUserId,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime);

    Integer countRecentSince(@Param("sinceTime") String sinceTime);

    List<ConsoleOperationLog> listRecent(@Param("limit") Integer limit);
}
