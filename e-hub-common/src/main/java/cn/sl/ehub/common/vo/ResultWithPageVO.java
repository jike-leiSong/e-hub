package cn.sl.ehub.common.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@ApiModel("带分页参数的返回类")
@Data
public class ResultWithPageVO<T> extends ResultVO<T> {
    private Integer pageIndex;
    private Integer pageSize;
    private Integer total;

    public static <T> ResultWithPageVO<T> success4Page(T data, Integer pageIndex, Integer pageSize, Integer total) {
        ResultWithPageVO<T> result = new ResultWithPageVO<>();
        result.setCode(HttpStatus.OK.value());
        result.setMsg(HttpStatus.OK.getReasonPhrase());
        result.setData(data);
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        result.setTotal(total);
        return result;
    }
}