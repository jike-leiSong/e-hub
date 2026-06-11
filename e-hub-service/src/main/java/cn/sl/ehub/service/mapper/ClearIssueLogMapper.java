package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ClearIssueLog;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ClearIssueLogMapper extends Mapper<ClearIssueLog> {

    List<ClearIssueLog> getLastIssueDate(@Param("startDate") String startDate, @Param("endDate") String endDate);
}