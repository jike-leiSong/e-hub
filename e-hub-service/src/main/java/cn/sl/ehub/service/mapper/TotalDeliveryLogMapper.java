package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.TotalDeliveryLog;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TotalDeliveryLogMapper extends Mapper<TotalDeliveryLog> {
    List<TotalDeliveryLog> selectLogsByCreateTimeAsc(@Param("beginTime")String beginTime, @Param("endTime")String endTime);
    List<TotalDeliveryLog> selectLogsByCreateTimeDesc(@Param("beginTime")String beginTime, @Param("endTime")String endTime);
    List<TotalDeliveryLog> selectLogsByDeliveryTimeAsc(@Param("beginTime")String beginTime, @Param("endTime")String endTime);
    List<TotalDeliveryLog> selectLogsByDeliveryTimeDesc(@Param("beginTime")String beginTime, @Param("endTime")String endTime);
}