package cn.sl.ehub.service.vo;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "aggregator_sms")
public class AggregatorSms {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    /**
     * 1 聚合商 0 企业
     */
    @Column(name = "role")
    private String role;

    /**
     * 企业id
     */
    @Column(name = "ent_id")
    private String entId;

    /**
     * 短信可接收标识
     * 1 可接收
     * 0 不可接收
     */
    @Column(name = "acceptable")
    private Integer acceptable;

    public Integer getQueryFlag() {
        return queryFlag;
    }

    public void setQueryFlag(Integer queryFlag) {
        this.queryFlag = queryFlag;
    }

    /**
     * 查询标志
     */
    @Column(name = "query_flag")
    private Integer queryFlag;

    /**
     * 电网编码
     */
    @Column(name = "grid_code")
    private String gridCode;


    public String getGridCode() {
        return gridCode;
    }

    public void setGridCode(String gridCode) {
        this.gridCode = gridCode;
    }

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

    /**
     * 获取企业id
     *
     * @return ent_id - 企业id
     */
    public String getEntId() {
        return entId;
    }

    /**
     * 设置企业id
     *
     * @param entId 企业id
     */
    public void setEntId(String entId) {
        this.entId = entId;
    }

    public Integer getAcceptable() {
        return acceptable;
    }

    public void setAcceptable(Integer acceptable) {
        this.acceptable = acceptable;
    }
}