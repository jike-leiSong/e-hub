package cn.sl.ehub.service.dto;

import lombok.Data;

@Data
public class RetryIssueDTO {
    private String commandId;
    private String deviceId;
    private String controlValue;
    private String retryTime;
}
