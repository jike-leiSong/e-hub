package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotOriginDataReceiveReq {

    private List<IotOriginDataItem> dataList;
}
