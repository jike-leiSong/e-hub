package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotCimDataReceiveReq {

    private String userKey;

    private String entId;

    private List<IotCimDataItem> data;
}
