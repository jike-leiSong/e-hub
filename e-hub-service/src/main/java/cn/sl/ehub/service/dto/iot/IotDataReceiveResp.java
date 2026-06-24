package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IotDataReceiveResp {

    private Integer success = 0;

    private Integer fail = 0;

    private List<IotDataReceiveFailItem> failList = new ArrayList<>();

    public void addSuccess() {
        this.success = this.success + 1;
    }

    public void addFail(IotDataReceiveFailItem failItem) {
        this.fail = this.fail + 1;
        this.failList.add(failItem);
    }
}
