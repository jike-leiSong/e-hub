package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleUserMapper extends Mapper<ConsoleUser> {

    ConsoleUser getByUsername(@Param("username") String username);

    List<ConsoleUser> listCustomers(@Param("keyword") String keyword);

    List<ConsoleUser> page(@Param("keyword") String keyword,
                           @Param("tenantId") String tenantId,
                           @Param("status") Integer status);

    List<ConsoleUser> listByUserIds(@Param("userIds") List<String> userIds);

    ConsoleUser getByUserId(@Param("userId") String userId);

    Integer countByUsername(@Param("username") String username,
                            @Param("excludeUserId") String excludeUserId);
}
