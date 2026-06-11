package cn.sl.ehub.service.vo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "iot_cmd_set_log")
public class IotCmdSetLog {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 链路跟踪id
     */
    @Column(name = "request_id")
    private String requestId;

    /**
     * iot下发返回状态码
     */
    @Column(name = "iot_code")
    private Integer iotCode;

    /**
     * iot下发返回结果
     */
    @Column(name = "iot_result")
    private String iotResult;

    /**
     * 指令下发时间
     */
    @Column(name = "cmd_set_time")
    private Date cmdSetTime;

    @Column(name = "system_code")
    private String systemCode;

    /**
     * iot 下发cimId
     */
    @Column(name = "cim_id")
    private String cimId;

    /**
     * 下发指令
     */
    @Column(name = "metric_code")
    private String metricCode;

    /**
     * 下发值
     */
    @Column(name = "target_value")
    private String targetValue;

    /**
     * 回调时间
     */
    @Column(name = "call_back_time")
    private Date callBackTime;

    /**
     * 回调结果
     */
    @Column(name = "call_back_result")
    private String callBackResult;

    /**
     * 回调状态
     */
    @Column(name = "call_back_code")
    private Integer callBackCode;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 物联返回uid
     */
    @Column(name = "u_id")
    private String uId;

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
     * 获取链路跟踪id
     *
     * @return request_id - 链路跟踪id
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置链路跟踪id
     *
     * @param requestId 链路跟踪id
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取iot下发返回状态码
     *
     * @return iot_code - iot下发返回状态码
     */
    public Integer getIotCode() {
        return iotCode;
    }

    /**
     * 设置iot下发返回状态码
     *
     * @param iotCode iot下发返回状态码
     */
    public void setIotCode(Integer iotCode) {
        this.iotCode = iotCode;
    }

    /**
     * 获取iot下发返回结果
     *
     * @return iot_result - iot下发返回结果
     */
    public String getIotResult() {
        return iotResult;
    }

    /**
     * 设置iot下发返回结果
     *
     * @param iotResult iot下发返回结果
     */
    public void setIotResult(String iotResult) {
        this.iotResult = iotResult;
    }

    /**
     * 获取指令下发时间
     *
     * @return cmd_set_time - 指令下发时间
     */
    public Date getCmdSetTime() {
        return cmdSetTime;
    }

    /**
     * 设置指令下发时间
     *
     * @param cmdSetTime 指令下发时间
     */
    public void setCmdSetTime(Date cmdSetTime) {
        this.cmdSetTime = cmdSetTime;
    }

    /**
     * @return system_code
     */
    public String getSystemCode() {
        return systemCode;
    }

    /**
     * @param systemCode
     */
    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    /**
     * 获取iot 下发cimId
     *
     * @return cim_id - iot 下发cimId
     */
    public String getCimId() {
        return cimId;
    }

    /**
     * 设置iot 下发cimId
     *
     * @param cimId iot 下发cimId
     */
    public void setCimId(String cimId) {
        this.cimId = cimId;
    }

    /**
     * 获取下发指令
     *
     * @return metric_code - 下发指令
     */
    public String getMetricCode() {
        return metricCode;
    }

    /**
     * 设置下发指令
     *
     * @param metricCode 下发指令
     */
    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    /**
     * 获取下发值
     *
     * @return target_value - 下发值
     */
    public String getTargetValue() {
        return targetValue;
    }

    /**
     * 设置下发值
     *
     * @param targetValue 下发值
     */
    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    /**
     * 获取回调时间
     *
     * @return call_back_time - 回调时间
     */
    public Date getCallBackTime() {
        return callBackTime;
    }

    /**
     * 设置回调时间
     *
     * @param callBackTime 回调时间
     */
    public void setCallBackTime(Date callBackTime) {
        this.callBackTime = callBackTime;
    }

    /**
     * 获取回调结果
     *
     * @return call_back_result - 回调结果
     */
    public String getCallBackResult() {
        return callBackResult;
    }

    /**
     * 设置回调结果
     *
     * @param callBackResult 回调结果
     */
    public void setCallBackResult(String callBackResult) {
        this.callBackResult = callBackResult;
    }

    /**
     * 获取回调状态
     *
     * @return call_back_code - 回调状态
     */
    public Integer getCallBackCode() {
        return callBackCode;
    }

    /**
     * 设置回调状态
     *
     * @param callBackCode 回调状态
     */
    public void setCallBackCode(Integer callBackCode) {
        this.callBackCode = callBackCode;
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
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取物联返回uid
     *
     * @return u_id - 物联返回uid
     */
    public String getuId() {
        return uId;
    }

    /**
     * 设置物联返回uid
     *
     * @param uId 物联返回uid
     */
    public void setuId(String uId) {
        this.uId = uId;
    }
}