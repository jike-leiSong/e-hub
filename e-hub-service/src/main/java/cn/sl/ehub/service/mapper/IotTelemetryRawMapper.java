package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.IotTelemetryRaw;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface IotTelemetryRawMapper extends Mapper<IotTelemetryRaw> {

    int insertRaw(IotTelemetryRaw record);
}
