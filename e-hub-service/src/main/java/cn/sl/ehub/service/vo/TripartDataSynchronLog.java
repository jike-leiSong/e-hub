package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "tripart_data_synchron_log")
public class TripartDataSynchronLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 第三方名称
     */
    @Column(name = "thipart_name")
    private String thipartName;

    /**
     * 第三方编码
     */
    @Column(name = "thipart_system_code")
    private String thipartSystemCode;

    /**
     * 同步数据
     */
    @Column(name = "data")
    private String data;

    /**
     * 1 成功 0 失败
     */
    @Column(name = "statue")
    private String statue;

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
     * 获取第三方名称
     *
     * @return thipart_name - 第三方名称
     */
    public String getThipartName() {
        return thipartName;
    }

    /**
     * 设置第三方名称
     *
     * @param thipartName 第三方名称
     */
    public void setThipartName(String thipartName) {
        this.thipartName = thipartName;
    }

    /**
     * 获取第三方编码
     *
     * @return thipart_system_code - 第三方编码
     */
    public String getThipartSystemCode() {
        return thipartSystemCode;
    }

    /**
     * 设置第三方编码
     *
     * @param thipartSystemCode 第三方编码
     */
    public void setThipartSystemCode(String thipartSystemCode) {
        this.thipartSystemCode = thipartSystemCode;
    }

    /**
     * 获取同步数据
     *
     * @return data - 同步数据
     */
    public String getData() {
        return data;
    }

    /**
     * 设置同步数据
     *
     * @param data 同步数据
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * 获取1 成功 0 失败
     *
     * @return statue - 1 成功 0 失败
     */
    public String getStatue() {
        return statue;
    }

    /**
     * 设置1 成功 0 失败
     *
     * @param statue 1 成功 0 失败
     */
    public void setStatue(String statue) {
        this.statue = statue;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}