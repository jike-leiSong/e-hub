package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "total_delivery_log")
public class TotalDeliveryLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 组号 空时表示上送，具体有值表示补招
     */
    @Column(name = "group_no")
    private String groupNo;

    /**
     * 值
     */
    @Column(name = "value")
    private String value;

    /**
     * 上送状态 华北返回结果
     */
    @Column(name = "delivery_status")
    private String deliveryStatus;

    /**
     * 时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 华北补招时下发10位时间戳
     */
    @Column(name = "issue_time")
    private Long issueTime;

    /**
     * 手动补招时间
     */
    @Column(name = "delivery_time")
    private Date deliveryTime;

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
     * 获取组号 空时表示上送，具体有值表示补招
     *
     * @return group_no
     */
    public String getGroupNo() {
        return groupNo;
    }

    /**
     * 设置组号 空时表示上送，具体有值表示补招
     *
     * @param groupNo
     */
    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    /**
     * 获取值
     *
     * @return value - 值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取上送状态 华北返回结果
     *
     * @return delivery_status - 上送状态 华北返回结果
     */
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * 设置上送状态 华北返回结果
     *
     * @param deliveryStatus 上送状态 华北返回结果
     */
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * 获取时间
     *
     * @return create_time - 时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置时间
     *
     * @param createTime 时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取下发时间戳
     *
     * @return issue_time - 下发时间戳
     */
    public Long getIssueTime() {
        return issueTime;
    }

    /**
     * 设置下发时间戳
     *
     * @param issueTime 下发时间戳
     */
    public void setIssueTime(Long issueTime) {
        this.issueTime = issueTime;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}