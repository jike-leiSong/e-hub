package cn.sl.ehub.common.exception;

import cn.sl.ehub.common.vo.guangzhou.GuangZhouResultVO;
import cn.sl.ehub.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ParamException.class)
    public ResultVO paramException(ParamException e) {
        return ResultVO.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultVO missingServletRequestParameterException(MissingServletRequestParameterException e) {
        String parameterName = e.getParameterName();
        return ResultVO.fail(HttpStatus.BAD_REQUEST.value(), "参数" + parameterName + "为空");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BaseException.class)
    public ResultVO baseException(BaseException e) {
        return ResultVO.fail(e.getCode(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ResultVO commonException(Exception e) {
        log.info("服务异常", e);
        return ResultVO.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务异常");
    }
}
