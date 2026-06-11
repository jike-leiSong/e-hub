package cn.sl.ehub.service.vo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import javax.persistence.*;

@Table(name = "clear_issue_log")
public class ClearIssueLog {
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
     * 出清日期
     */
    @Column(name = "clear_Date")
    @JSONField(format="yyyy-MM-dd")
    private Date clearDate;

    @Column(name = "request_id")
    private String requestId;

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
     * 获取下发值
     *
     * @return cmd_data - 下发值
     */
    public String getCmdData() {
        return cmdData;
    }

    /**
     * 设置下发值
     *
     * @param cmdData 下发值
     */
    public void setCmdData(String cmdData) {
        this.cmdData = cmdData;
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
     * @return request_id
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public Date getClearDate() {
        return clearDate;
    }

    public void setClearDate(Date clearDate) {
        this.clearDate = clearDate;
    }
}