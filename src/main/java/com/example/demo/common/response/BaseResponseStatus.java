package com.example.demo.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 200 : 요청 성공
     */
    SUCCESS(true, HttpStatus.OK.value(), "요청에 성공하였습니다."),
    NO_CONTENT(true, HttpStatus.NO_CONTENT.value(), "삭제 성공하였습니다."),

    /**
     * 400 : Request, Response 오류
     */

    USERS_EMPTY_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "이메일을 입력해주세요."),
    TEST_EMPTY_COMMENT(false, HttpStatus.BAD_REQUEST.value(), "코멘트를 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다."),
    POST_TEST_EXISTS_MEMO(false, HttpStatus.BAD_REQUEST.value(), "중복된 메모입니다."),

    RESPONSE_ERROR(false, HttpStatus.NOT_FOUND.value(), "값을 불러오는데 실패하였습니다."),

    DUPLICATED_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다."),
    INVALID_MEMO(false, HttpStatus.NOT_FOUND.value(), "존재하지 않는 메모입니다."),
    FAILED_TO_LOGIN(false, HttpStatus.NOT_FOUND.value(), "없는 아이디거나 비밀번호가 틀렸습니다."),
    EMPTY_JWT(false, HttpStatus.UNAUTHORIZED.value(), "JWT를 입력해주세요."),
    INVALID_JWT(false, HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false, HttpStatus.FORBIDDEN.value(), "권한이 없는 유저의 접근입니다."),
    NOT_FIND_USER(false, HttpStatus.NOT_FOUND.value(), "일치하는 유저가 없습니다."),
    INVALID_OAUTH_TYPE(false, HttpStatus.BAD_REQUEST.value(), "알 수 없는 소셜 로그인 형식입니다."),
    INVALID_ID(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않는 ID 입니다."),
    INVALID_STATE(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않는 State 입니다."),
    INVALID_PAGE(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않는 페이지입니다."),
    INVALID_PAGE_SIZE(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않는 페이지 크기입니다."),
    INVALID_DATE(false, HttpStatus.BAD_REQUEST.value(), "가입일은 오늘을 포함한 이전으로 설정해야 합니다."),
    DELETED_USER(false, HttpStatus.BAD_REQUEST.value(), "이미 삭제된 유저입니다."),
    NOT_SOFT_DELETED_USER(false, HttpStatus.BAD_REQUEST.value(), "아직 Soft Deleting 되지 않은 유저입니다."),
    TOO_SOON_TO_DELETE(false, HttpStatus.BAD_REQUEST.value(), "7일이 지난 후 Hard Deleting이 가능합니다."),
    INVALID_TARGET_USER_ID(false, HttpStatus.BAD_REQUEST.value(),
        "대상 사용자 ID가 유효하지 않습니다. 대상 사용자 ID는 1 이상이어야 합니다."),
    INVALID_DATE_RANGE(false, HttpStatus.BAD_REQUEST.value(), "종료 날짜가 시작 날짜보다 앞설 수는 없습니다."),
    INVALID_REQUEST_PARAM(false, HttpStatus.BAD_REQUEST.value(), "요청 파라미터가 올바르지 않습니다."),
    AUDIT_DATA_NOT_FOUND(false, HttpStatus.NOT_FOUND.value(), "기록을 찾을 수 없습니다."),
    INVALID_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "올바르지 않을 이메일 형식입니다."),
    MISSING_PARAMETER(false, HttpStatus.BAD_REQUEST.value(), "필수 파라미터가 누락되었습니다."),
    REVISION_ID_TOO_LARGE(false,HttpStatus.BAD_REQUEST.value(), "revision ID값이 너무 큽니다."),

    /**
     * 500 :  Database, Server 오류
     */
    DATABASE_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버와의 연결에 실패하였습니다."),
    PASSWORD_ENCRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "비밀번호 복호화에 실패하였습니다."),


    MODIFY_FAIL_USERNAME(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "유저네임 수정 실패"),
    DELETE_FAIL_USERNAME(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "유저 삭제 실패"),
    DELETE_FAIL_USERID(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "유저 삭제 실패"),
    MODIFY_FAIL_MEMO(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "메모 수정 실패"),
    AUDIT_SYSTEM_ERROR(false,HttpStatus.INTERNAL_SERVER_ERROR.value(),"감사 시스템 에러 발생하였습니다." ),

    UNEXPECTED_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "예상치 못한 에러가 발생했습니다.");


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
