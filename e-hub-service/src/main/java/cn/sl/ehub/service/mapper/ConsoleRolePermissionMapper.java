package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleRolePermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleRolePermissionMapper extends Mapper<ConsoleRolePermission> {

    List<ConsoleRolePermission> listByRoleIds(@Param("roleIds") List<String> roleIds);

    List<ConsoleRolePermission> listByRoleId(@Param("roleId") String roleId);

    int deleteByRoleId(@Param("roleId") String roleId);
}
