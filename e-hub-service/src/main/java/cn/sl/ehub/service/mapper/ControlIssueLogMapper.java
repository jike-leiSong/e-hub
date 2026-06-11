package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ControlIssueLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 控制下发日志Mapper
 * @author sl
 * @date 2026-06-03
 */
public interface ControlIssueLogMapper extends Mapper<ControlIssueLog> {

    /**
     * 根据组号查询最新的日志
     */
    @Select("SELECT * FROM control_issue_log WHERE group_no = #{groupNo} ORDER BY create_time DESC LIMIT 1")
    ControlIssueLog selectLastLogByGroupNo(@Param("groupNo") String groupNo);

    /**
     * 根据组号和时间范围查询最新的日志
     */
    @Select("SELECT * FROM control_issue_log WHERE group_no = #{groupNo} AND create_time >= #{startTime} ORDER BY create_time DESC LIMIT 1")
    ControlIssueLog selectLastLogByGroupNoAndTime(@Param("groupNo") String groupNo, @Param("startTime") Date startTime);
}
