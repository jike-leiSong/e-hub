package cn.sl.ehub.upstream.exception;

import cn.sl.ehub.common.vo.ResultVO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常处理
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public ResultVO<String> controlExceptionHandler(BaseException e) {
        return ResultVO.fail(e.getCode(), e.getMessage());
    }
}
