package cn.sl.ehub.common.vo.guangzhou.jintian;

import cn.sl.ehub.common.enums.guangzhou.jintian.StatusCodeGuangZhouJinTian;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @Description: 广州返回实体
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("返回类")
public class GuangZhouJinTianResultVO<T> implements Serializable {

    private static final long serialVersionUID = -8620159296252883218L;
    /**
     * 错误码.
     */
    @ApiModelProperty("错误码")
    private Integer status;

    /**
     * 提示信息.
     */
    @ApiModelProperty("提示信息")
    private String message;

    /**
     * 具体内容.
     */
    @ApiModelProperty("具体内容")
    private T data;

    public GuangZhouJinTianResultVO() {
    }

    public GuangZhouJinTianResultVO(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public GuangZhouJinTianResultVO(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> GuangZhouJinTianResultVO success() {
        return new GuangZhouJinTianResultVO(StatusCodeGuangZhouJinTian.SUCCESS.getCode(), StatusCodeGuangZhouJinTian.SUCCESS.getMsg());
    }

    public static <T> GuangZhouJinTianResultVO<T> success(T data) {
        return new GuangZhouJinTianResultVO<>(StatusCodeGuangZhouJinTian.SUCCESS.getCode(), StatusCodeGuangZhouJinTian.SUCCESS.getMsg(), data);
    }

    public static <T> GuangZhouJinTianResultVO success(T data, String msg) {
        return new GuangZhouJinTianResultVO(StatusCodeGuangZhouJinTian.SUCCESS.getCode(), msg, data);
    }

    public static <T> GuangZhouJinTianResultVO fail() {
        return new GuangZhouJinTianResultVO(StatusCodeGuangZhouJinTian.ERROR.getCode(), StatusCodeGuangZhouJinTian.ERROR.getMsg());
    }

    public static <T> GuangZhouJinTianResultVO fail(String msg) {
        return new GuangZhouJinTianResultVO(StatusCodeGuangZhouJinTian.ERROR.getCode(), msg);
    }

    public static <T> GuangZhouJinTianResultVO fail(Integer status, String msg) {
        return new GuangZhouJinTianResultVO(status, msg);
    }
}