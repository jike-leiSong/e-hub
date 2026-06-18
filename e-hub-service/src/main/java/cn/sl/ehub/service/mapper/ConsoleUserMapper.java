package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ConsoleUserMapper extends Mapper<ConsoleUser> {

    ConsoleUser getByUsername(@Param("username") String username);
}
