package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 华北下发数据实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("华北下发数据实体")
public class RemoteResp {

    private String remoteId;
    private String remoteName;
    private List<List<Map<String, String>>> data;
}
