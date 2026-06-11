package cn.sl.ehub.service.vo;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tripart_service_alert")
public class TripartServiceAlert {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 第三方编码
     */
    @Column(name = "tripart_code")
    private String tripartCode;

    /**
     * 姓名
     */
    @Column(name = "name")
    private String name;

    /**
     * 手机号
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 删除标识 0 删除 
     */
    @Column(name = "status")
    private String status;

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
     * 获取第三方编码
     *
     * @return tripart_code - 第三方编码
     */
    public String getTripartCode() {
        return tripartCode;
    }

    /**
     * 设置第三方编码
     *
     * @param tripartCode 第三方编码
     */
    public void setTripartCode(String tripartCode) {
        this.tripartCode = tripartCode;
    }

    /**
     * 获取姓名
     *
     * @return name - 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置姓名
     *
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取手机号
     *
     * @return phone - 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号
     *
     * @param phone 手机号
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取删除标识 0 删除 
     *
     * @return status - 删除标识 0 删除 
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置删除标识 0 删除 
     *
     * @param status 删除标识 0 删除 
     */
    public void setStatus(String status) {
        this.status = status;
    }
}