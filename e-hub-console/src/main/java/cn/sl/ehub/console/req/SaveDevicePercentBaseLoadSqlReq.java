package cn.sl.ehub.console.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 生成设备比例基线负荷Sql请求实体
 *
 * @Author sl
 * @phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("生成设备比例基线负荷Sql请求实体")
public class SaveDevicePercentBaseLoadSqlReq {

    private List<Double> powerList;
    private List<cn.sl.ehub.console.req.DevicePercentReq> devicePercentReqList;
}
