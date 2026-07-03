package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleUserRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleUserRoleMapper extends Mapper<ConsoleUserRole> {

    List<ConsoleUserRole> listByUserIds(@Param("userIds") List<String> userIds);

    List<ConsoleUserRole> listByUserId(@Param("userId") String userId);

    int deleteByUserId(@Param("userId") String userId);
}
