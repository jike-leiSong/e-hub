package cn.sl.ehub.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 总加补招/单体补招下发实体
 * @Author sl
 * @Date 2026-05-28
 * {
 * "remoteId": "91855fca-7186-4dea-bf86-ce88b3626d9d",
 * "remoteName": "HUABEISG",
 * "groupNo":"组号",
 * "type":"total",
 * "timestamp":"10位秒级时间戳"
 * }
 * <p>
 * 返回值：
 * {
 * "组号-点号": 值,
 * "组号-点号": 值,
 * "组号-点号": 值
 * }
 */
@Data
@NoArgsConstructor
public class RetryIssueDTO {

    private String remoteId;

    private String remoteName;

    private String group;

    private String type;

    //10位秒级时间戳
    private String timestamp;

}
