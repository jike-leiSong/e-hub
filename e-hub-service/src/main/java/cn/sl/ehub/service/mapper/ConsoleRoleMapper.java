package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleRoleMapper extends Mapper<ConsoleRole> {

    List<ConsoleRole> page(@Param("keyword") String keyword,
                           @Param("platformType") String platformType,
                           @Param("status") Integer status);

    ConsoleRole getByRoleId(@Param("roleId") String roleId);

    Integer countByRoleCode(@Param("platformType") String platformType,
                            @Param("roleCode") String roleCode,
                            @Param("excludeRoleId") String excludeRoleId);

    Integer countAll();

    List<ConsoleRole> listByRoleIds(@Param("roleIds") List<String> roleIds);
}
