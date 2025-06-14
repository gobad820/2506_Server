package com.example.demo.common.exceptions;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<BaseResponse<BaseResponseStatus>> BaseExceptionHandle(
        BaseException exception) {
        BaseResponseStatus status = exception.getStatus();
        if (status.getCode() >= 500) {
            log.error("Server error occurred: {}", exception.getMessage(), exception);
        } else if (status.getCode() >= 400) {
            log.warn("Client error occurred: {}", exception.getMessage());
        } else {
            log.info("Business logic handled: {}", exception.getMessage());
        }
        HttpStatus httpStatus = HttpStatus.valueOf(status.getCode());
        BaseResponse<BaseResponseStatus> response = new BaseResponse<>(status);
        return new ResponseEntity<>(response, httpStatus);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<BaseResponseStatus> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
            .map(err -> err.getField() + " : " + err.getDefaultMessage())
            .collect(Collectors.toList());
        logFailureWarning(errors, "validation");
        return new BaseResponse<>(BaseResponseStatus.INVALID_REQUEST_PARAM);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<BaseResponseStatus> handleConstraintViolationException(
        ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage()).collect(Collectors.toList());

        logFailureWarning(errors, "Constraining violation");

      boolean isUserIdError = errors.stream()
         .anyMatch(error -> error.matches("(?i).*\\b(user_?id|userid)\\b.*"));

        if (isUserIdError) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_ID);
        }
        return new BaseResponse<>(BaseResponseStatus.INVALID_REQUEST_PARAM);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<BaseResponseStatus> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception) {
        log.warn(
            "Type conversion error: parameter '{}' with value '{}' could not be converted to type '{}'",
            exception.getName(),
            exception.getValue(),
            exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName()
                : "unknown");

        if (exception.getRequiredType() != null && exception.getRequiredType().isEnum()) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_STATE);
        }

       if (exception.getName().matches("(?i)^(user_?id|userid|id)$")) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_ID);
        }

        return new BaseResponse<>(BaseResponseStatus.INVALID_REQUEST_PARAM);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<BaseResponseStatus> handleIllegalArgumentException(
        IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());
        return new BaseResponse<>(BaseResponseStatus.INVALID_REQUEST_PARAM);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public BaseResponse<BaseResponseStatus> handleDataAccessException(
        org.springframework.dao.DataAccessException exception) {
        log.error("Database error occurred", exception);
        return new BaseResponse<>(BaseResponseStatus.DATABASE_ERROR);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
        ExpiredJwtException.class,
        UnsupportedJwtException.class,
        MalformedJwtException.class,
        SignatureException.class,
    })
    public BaseResponse<BaseResponseStatus> handleJwtException(Exception exception) {
        log.warn("JWT authentication failed: {}", exception.getMessage());
        return new BaseResponse<>(BaseResponseStatus.INVALID_JWT);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    public BaseResponse<BaseResponseStatus> handleNullPointerException(
        NullPointerException exception) {
        log.error("NullPointerException occurred",exception);
        return new BaseResponse<>(BaseResponseStatus.SERVER_ERROR);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BaseResponse<BaseResponseStatus> ExceptionHandle(Exception exception) {
        log.error("Exception has occured. ", exception);
        return new BaseResponse<>(BaseResponseStatus.UNEXPECTED_ERROR);
    }


    private static void logFailureWarning(List<String> errors, String msg) {
        String input = msg + " failed ";
        String firstErr = errors.isEmpty() ? input : errors.get(0);
        log.warn(input + firstErr);
    }
}
