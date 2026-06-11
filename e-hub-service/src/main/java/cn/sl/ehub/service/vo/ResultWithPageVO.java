package cn.sl.ehub.service.vo;

import lombok.Data;
import cn.sl.ehub.common.enums.StatusCode;

import java.util.List;

@Data
public class ResultWithPageVO<T> {
    private Integer code;
    private String msg;
    private String message;
    private T data;
    private Integer total;
    private Integer pageNum;
    private Integer pageSize;

    public static <T> ResultWithPageVO<T> success4Page(T data, Integer total, Integer pageNum, Integer pageSize) {
        ResultWithPageVO<T> result = new ResultWithPageVO<>();
        result.setCode(StatusCode.SUCCESS.getCode());
        result.setMsg(StatusCode.SUCCESS.getMsg());
        result.setMessage(StatusCode.SUCCESS.getMsg());
        result.setData(data);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
}
