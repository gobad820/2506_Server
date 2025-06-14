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
    protected ResponseEntity<BaseResponse<Void>> BaseExceptionHandle(
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
        BaseResponse<Void> response = new BaseResponse<>(status);
        return new ResponseEntity<>(response, httpStatus);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Void> handleMethodArgumentNotValidException(
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
    public BaseResponse<Void> handleConstraintViolationException(
        ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage()).collect(Collectors.toList());

        logFailureWarning(errors, "Constraint violation");

        BaseResponseStatus baseResponseStatus = specificValidationError(errors);
        return new BaseResponse<>(baseResponseStatus);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<Void> handleTypeMismatch(
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
    public BaseResponse<Void> handleIllegalArgumentException(
        IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());
        return new BaseResponse<>(BaseResponseStatus.INVALID_REQUEST_PARAM);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public BaseResponse<Void> handleDataAccessException(
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
    public BaseResponse<Void> handleJwtException(Exception exception) {
        log.warn("JWT authentication failed: {}", exception.getMessage());
        return new BaseResponse<>(BaseResponseStatus.INVALID_JWT);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    public BaseResponse<Void> handleNullPointerException(
        NullPointerException exception) {
        log.error("NullPointerException occurred", exception);
        return new BaseResponse<>(BaseResponseStatus.SERVER_ERROR);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BaseResponse<Void> handleException(Exception exception) {
        log.error("Exception has occurred. ", exception);
        return new BaseResponse<>(BaseResponseStatus.UNEXPECTED_ERROR);
    }


    private static void logFailureWarning(List<String> errors, String msg) {
        String input = msg + " failed ";
        String firstErr = errors.isEmpty() ? input : errors.get(0);
        log.warn(input + firstErr);
    }

    private BaseResponseStatus specificValidationError(List<String> errors) {
        for (String error : errors) {
            String lowerCase = error.toLowerCase();

            if (lowerCase.matches(".*\\b(target_?user_?id|targetuserid)\\b.*")) {
                if (lowerCase.contains("1 이상") || lowerCase.contains("must be greater")) {
                    return BaseResponseStatus.INVALID_TARGET_USER_ID;
                } else if (lowerCase.contains("너무 큽니다")) {
                    return BaseResponseStatus.INVALID_TARGET_USER_ID;
                }
                return BaseResponseStatus.INVALID_TARGET_USER_ID;
            }
        }
        return BaseResponseStatus.INVALID_REQUEST_PARAM;
    }
}
