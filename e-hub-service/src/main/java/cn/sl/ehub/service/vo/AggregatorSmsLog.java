package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "aggregator_sms_log")
public class AggregatorSmsLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "status")
    private String status;

    /**
     * 1 聚合商 0 企业
     */
    @Column(name = "role")
    private String role;

    /**
     * 短信内容
     */
    @Column(name = "content")
    private String content;

    /**
     * 国网编码
     */
    @Column(name = "grid_code")
    private String gridCode;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
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
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取1 聚合商 0 企业
     *
     * @return role - 1 聚合商 0 企业
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置1 聚合商 0 企业
     *
     * @param role 1 聚合商 0 企业
     */
    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGridCode() {
        return gridCode;
    }

    public void setGridCode(String gridCode) {
        this.gridCode = gridCode;
    }
}