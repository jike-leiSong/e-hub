package cn.sl.ehub.service.dto;

import lombok.Data;

@Data
public class ControlIssueDTO {
    private String commandId;
    private String deviceId;
    private String controlValue;
    private String issueTime;
}
