package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleTenantProduct;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleTenantProductMapper extends Mapper<ConsoleTenantProduct> {

    List<ConsoleTenantProduct> listByTenantId(@Param("tenantId") String tenantId);

    List<ConsoleTenantProduct> listByTenantIds(@Param("tenantIds") List<String> tenantIds);

    Integer countEnabledTenants();

    int deleteByTenantId(@Param("tenantId") String tenantId);
}
