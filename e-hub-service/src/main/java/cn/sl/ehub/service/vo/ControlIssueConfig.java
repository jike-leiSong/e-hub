package cn.sl.ehub.service.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 控制下发配置
 * @author sl
 * @date 2026-06-04
 */
@Data
@Table(name = "control_issue_config")
public class ControlIssueConfig {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "remote_id")
    private String remoteId;

    @Column(name = "config_data")
    private String configData;
}
