package cn.sl.ehub.service.vo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import javax.persistence.*;

/**
 * 控制下发日志
 * @author sl
 * @date 2026-06-03
 */
@Table(name = "control_issue_log")
public class ControlIssueLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 组号
     */
    @Column(name = "group_no")
    private String groupNo;

    /**
     * 下发值
     */
    @Column(name = "cmd_data")
    private String cmdData;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 下发日期
     */
    @Column(name = "issue_date")
    @JSONField(format="yyyy-MM-dd")
    private Date issueDate;

    @Column(name = "request_id")
    private String requestId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getCmdData() {
        return cmdData;
    }

    public void setCmdData(String cmdData) {
        this.cmdData = cmdData;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
