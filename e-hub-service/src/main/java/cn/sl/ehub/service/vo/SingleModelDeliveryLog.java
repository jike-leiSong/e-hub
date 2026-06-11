package cn.sl.ehub.service.vo;

import javax.persistence.*;
import java.util.Date;

@Table(name = "single_model_delivery_log")
public class SingleModelDeliveryLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 文件名
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 文件内容
     */
    @Column(name = "file_byte")
    private String fileByte;

    /**
     * 上送状态 华北返回结果
     */
    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "create_time")
    private Date createTime;

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
     * 获取文件名
     *
     * @return file_name - 文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     *
     * @param fileName 文件名
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
}