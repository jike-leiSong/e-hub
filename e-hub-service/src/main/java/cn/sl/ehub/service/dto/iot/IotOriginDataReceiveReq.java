package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.List;

@Data
public class IotOriginDataReceiveReq {

    private String userKey;

    private String entId;

    private List<IotOriginDataItem> dataList;
}
