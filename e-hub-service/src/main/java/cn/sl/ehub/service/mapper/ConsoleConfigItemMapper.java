package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleConfigItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleConfigItemMapper extends Mapper<ConsoleConfigItem> {

    List<ConsoleConfigItem> page(@Param("keyword") String keyword,
                                 @Param("configGroup") String configGroup,
                                 @Param("status") Integer status);

    ConsoleConfigItem getByConfigKey(@Param("configKey") String configKey);

    Integer countAll();
}
