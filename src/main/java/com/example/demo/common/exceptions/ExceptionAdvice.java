package com.example.demo.common.exceptions;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public BaseResponse<BaseResponseStatus> BaseExceptionHandle(BaseException exception) {
        log.warn("BaseException. error message: {}", exception.getMessage());
        return new BaseResponse<>(exception.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<BaseResponseStatus> handleConstraintViolation(
        ConstraintViolationException exception) {
        log.warn("Constraint violation error: {}", exception.getMessage());

        return new BaseResponse<>(BaseResponseStatus.INVALID_EMAIL);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public BaseResponse<BaseResponseStatus> handleMissingParameter(
        MissingServletRequestParameterException exception) {
        return new BaseResponse<>(BaseResponseStatus.MISSING_PARAMETER);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<BaseResponseStatus> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception) {
        log.warn("Type conversion error: {}", exception.getMessage());

        if (exception.getRequiredType() != null && exception.getRequiredType().isEnum()) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_STATE);
        }
        return new BaseResponse<>(BaseResponseStatus.INVALID_ID);
    }


    @ExceptionHandler(Exception.class)
    public BaseResponse<BaseResponseStatus> ExceptionHandle(Exception exception) {
        log.error("Exception has occured. ", exception);
        return new BaseResponse<>(BaseResponseStatus.UNEXPECTED_ERROR);
    }
}
