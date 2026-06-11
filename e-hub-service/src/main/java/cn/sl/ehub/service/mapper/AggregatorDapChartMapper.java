package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDapChart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AggregatorDapChartMapper  extends tk.mybatis.mapper.common.Mapper<AggregatorDapChart> {

    int batchInsert(@Param("list") List<AggregatorDapChart> list);

}