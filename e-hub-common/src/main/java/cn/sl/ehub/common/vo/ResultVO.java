package cn.sl.ehub.common.vo;

import cn.sl.ehub.common.exception.BaseException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@ApiModel("返回类")
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = 3068837394742385883L;

    /**
     * 错误码.
     */
    @ApiModelProperty("错误码")
    private Integer code;

    /**
     * 提示信息.
     */
    @ApiModelProperty("提示信息")
    private String msg;

    private String requestId;

    /**
     * 具体内容.
     */
    @ApiModelProperty("具体内容")
    private T data;

    public ResultVO() {
    }

    public ResultVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultVO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultVO(Integer code, String msg, String requestId) {
        this.code = code;
        this.msg = msg;
        this.requestId = requestId;
    }

    public ResultVO(Integer code, String msg, String requestId, T data) {
        this.code = code;
        this.msg = msg;
        this.requestId = requestId;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static <T> ResultVO success() {
        return new ResultVO(200, "成功");
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "成功", data);
    }

    public static <T> ResultVO success(T data, String msg) {
        return new ResultVO(200, msg, data);
    }

    public static <T> ResultVO success(T data, String msg, String requestId) {
        return new ResultVO(200, msg, requestId, data);
    }

    public static <T> ResultVO fail(Integer code) {
        return new ResultVO(code, "失败");
    }

    public static <T> ResultVO fail(Integer code, String msg) {
        return new ResultVO(code, msg);
    }

    public static <T> ResultVO fail(Integer code, String msg, String requestId) {
        return new ResultVO(code, msg, requestId);
    }

    public static <T> ResultVO<T> fail(BaseException ex) {
        return new ResultVO<>(ex.getCode(), ex.getMessage());
    }
}