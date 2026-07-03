package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.ConsoleDictType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ConsoleDictTypeMapper extends Mapper<ConsoleDictType> {

    List<ConsoleDictType> listAll(@Param("status") Integer status);

    ConsoleDictType getByDictType(@Param("dictType") String dictType);
}
