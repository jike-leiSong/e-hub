package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotCimDataReceiveReq {

    private List<IotCimDataItem> data;
}
