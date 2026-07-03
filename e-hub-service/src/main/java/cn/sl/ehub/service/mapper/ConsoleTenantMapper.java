package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleTenant;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleTenantMapper extends Mapper<ConsoleTenant> {

    List<ConsoleTenant> page(@Param("keyword") String keyword,
                             @Param("tenantType") String tenantType,
                             @Param("status") Integer status,
                             @Param("productCode") String productCode);

    ConsoleTenant getByTenantId(@Param("tenantId") String tenantId);

    Integer countByTenantName(@Param("tenantName") String tenantName,
                              @Param("excludeTenantId") String excludeTenantId);

    Integer countAll();

    Integer countActive();

    List<ConsoleTenant> listByTenantIds(@Param("tenantIds") List<String> tenantIds);
}
