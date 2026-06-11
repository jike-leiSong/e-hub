package cn.sl.ehub.common.vo.guangzhou;

import cn.sl.ehub.common.enums.guangzhou.StatusCodeGuangZhou;
import cn.sl.ehub.common.exception.BaseException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @Description: 广州返回实体
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("返回类")
public class GuangZhouResultVO<T> implements Serializable {

    private static final long serialVersionUID = 3068837394742385883L;

    /**
     * 错误码.
     */
    @ApiModelProperty("错误码")
    private String responseCode;

    /**
     * 提示信息.
     */
    @ApiModelProperty("提示信息")
    private String responseMessage;

    /**
     * 具体内容.
     */
    @ApiModelProperty("具体内容")
    private T responseData;

    public GuangZhouResultVO() {
    }

    public GuangZhouResultVO(String responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public GuangZhouResultVO(String responseCode, String responseMessage, T responseData) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseData = responseData;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public T getResponseData() {
        return responseData;
    }

    public void setResponseData(T responseData) {
        this.responseData = responseData;
    }

    public static <T> GuangZhouResultVO success() {
        return new GuangZhouResultVO(StatusCodeGuangZhou.RECEIVE_SUCCESS.getCode(), StatusCodeGuangZhou.RECEIVE_SUCCESS.getMsg());
    }

    public static <T> GuangZhouResultVO<T> success(T responseData) {
        return new GuangZhouResultVO<>(StatusCodeGuangZhou.RECEIVE_SUCCESS.getCode(), StatusCodeGuangZhou.RECEIVE_SUCCESS.getMsg(), responseData);
    }

    public static <T> GuangZhouResultVO success(T responseData, String responseMessage) {
        return new GuangZhouResultVO(StatusCodeGuangZhou.RECEIVE_SUCCESS.getCode(), responseMessage, responseData);
    }

    public static <T> GuangZhouResultVO fail() {
        return new GuangZhouResultVO(StatusCodeGuangZhou.RECEIVE_FAIL.getCode(), StatusCodeGuangZhou.RECEIVE_FAIL.getMsg());
    }

    public static <T> GuangZhouResultVO fail(String responseMessage) {
        return new GuangZhouResultVO(StatusCodeGuangZhou.RECEIVE_FAIL.getCode(), responseMessage);
    }

    public static <T> GuangZhouResultVO fail(String responseCode, String responseMessage) {
        return new GuangZhouResultVO(responseCode, responseMessage);
    }

    public static <T> GuangZhouResultVO<T> fail(BaseException ex) {
        return new GuangZhouResultVO<>(String.valueOf(ex.getCode()), ex.getMessage());
    }
}