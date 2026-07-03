package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsolePermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsolePermissionMapper extends Mapper<ConsolePermission> {

    List<ConsolePermission> listEnabled();

    List<ConsolePermission> listByCodes(@Param("permissionCodes") List<String> permissionCodes);
}
