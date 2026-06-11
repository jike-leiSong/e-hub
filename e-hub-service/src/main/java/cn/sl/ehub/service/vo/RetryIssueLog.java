package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "retry_issue_log")
public class RetryIssueLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 远程id
     */
    @Column(name = "remote_id")
    private String remoteId;

    /**
     * 远程名称
     */
    @Column(name = "remote_name")
    private String remoteName;

    /**
     * 组号
     */
    @Column(name = "group_no")
    private String groupNo;

    /**
     * 单体：single  总加：total
     */
    @Column(name = "type")
    private String type;

    /**
     * 十位时间戳
     */
    @Column(name = "timestamp")
    private Long timestamp;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "response")
    private String response;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取远程id
     *
     * @return remote_id - 远程id
     */
    public String getRemoteId() {
        return remoteId;
    }

    /**
     * 设置远程id
     *
     * @param remoteId 远程id
     */
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    /**
     * 获取远程名称
     *
     * @return remote_name - 远程名称
     */
    public String getRemoteName() {
        return remoteName;
    }

    /**
     * 设置远程名称
     *
     * @param remoteName 远程名称
     */
    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    /**
     * 获取组号
     *
     * @return group_no - 组号
     */
    public String getGroupNo() {
        return groupNo;
    }

    /**
     * 设置组号
     *
     * @param groupNo 组号
     */
    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    /**
     * 获取单体：single  总加：total
     *
     * @return type - 单体：single  总加：total
     */
    public String getType() {
        return type;
    }

    /**
     * 设置单体：single  总加：total
     *
     * @param type 单体：single  总加：total
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取十位时间戳
     *
     * @return timestamp - 十位时间戳
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置十位时间戳
     *
     * @param timestamp 十位时间戳
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response
     */
    public void setResponse(String response) {
        this.response = response;
    }
}