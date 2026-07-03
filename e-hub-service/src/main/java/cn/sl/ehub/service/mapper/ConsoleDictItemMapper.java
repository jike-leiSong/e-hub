package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleDictItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleDictItemMapper extends Mapper<ConsoleDictItem> {

    List<ConsoleDictItem> listByDictType(@Param("dictType") String dictType,
                                         @Param("status") Integer status);
}
