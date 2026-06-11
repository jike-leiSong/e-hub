package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "single_meas_delivery_log")
public class SingleMeasDeliveryLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 文件名称
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 文件内容
     */
    @Column(name = "file_byte")
    private String fileByte;

    /**
     * 上送状态 1 成功  0 失败
     */
    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "create_time")
    private Date createTime;

    /**
     * 下发时间戳
     */
    @Column(name = "issue_time")
    private Long issueTime;

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
     * 获取文件名称
     *
     * @return file_name - 文件名称
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名称
     *
     * @param fileName 文件名称
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件内容
     *
     * @return file_byte - 文件内容
     */
    public String getFileByte() {
        return fileByte;
    }

    /**
     * 设置文件内容
     *
     * @param fileByte 文件内容
     */
    public void setFileByte(String fileByte) {
        this.fileByte = fileByte;
    }

    /**
     * 获取上送状态 1 成功  0 失败
     *
     * @return delivery_status - 上送状态 1 成功  0 失败
     */
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * 设置上送状态 1 成功  0 失败
     *
     * @param deliveryStatus 上送状态 1 成功  0 失败
     */
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
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
}