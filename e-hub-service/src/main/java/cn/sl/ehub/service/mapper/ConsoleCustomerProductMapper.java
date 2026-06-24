package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleCustomerProduct;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleCustomerProductMapper extends Mapper<ConsoleCustomerProduct> {

    List<String> listEnabledProductCodes(@Param("userId") String userId,
                                         @Param("customerIds") List<String> customerIds);

    List<ConsoleCustomerProduct> listByUserIdsOrCustomerIds(@Param("userIds") List<String> userIds,
                                                            @Param("customerIds") List<String> customerIds);

    int deleteByUserId(@Param("userId") String userId);
}
