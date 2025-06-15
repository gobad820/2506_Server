package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.admin.model.UpdateUserReq;
import com.example.demo.src.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@Tag(name = "관리자", description = "관리자용 회원 관리 API")
@RequestMapping("/app/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "관리자용 회원 목록 조회", description = "관리자가 회원 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<Page<User>> getUsersInfoForAdmin(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기 (1-100)", example = "10")
        @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "검색할 회원 이름")
        @RequestParam(required = false) String name,
        @Parameter(description = "검색할 회원 ID")
        @RequestParam(required = false) Long userId,
        @Parameter(description = "가입일 필터 (yyyy-MM-dd'T'HH:mm:ss)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDate,
        @Parameter(description = "회원 상태 (ACTIVE, INACTIVE, DELETED)")
        @RequestParam(required = false) State state) {
        validateParameters(page, size);
        if (userId != null) {
            checkUserIdNegative(userId);
        }
        validateDate(createdDate);
        Page<User> users = adminService.getUsersInfo(page, size, name, userId, createdDate, state);
        return new BaseResponse<>(users);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "관리자용 회원 상세 조회", description = "관리자가 특정 회원의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<User> getSingleUserInfoForAdmin(
        @Parameter(description = "회원 ID", required = true, example = "1")
        @PathVariable Long userId) {
        validateUserId(userId);
        User userById = adminService.getUserById(userId);
        return new BaseResponse<>(userById);
    }

    @PostMapping("/users/inactivate/{userId}")
    @Operation(summary = "회원 비활성화", description = "회원을 비활성화 상태로 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비활성화 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<User> inActivateUser(
        @Parameter(description = "비활성화할 회원 ID", required = true, example = "1")
        @PathVariable Long userId) {
        validateUserId(userId);
        User user = adminService.changeUserState(userId, State.INACTIVE);
        return new BaseResponse<>(user);
    }

    @PatchMapping("/users/soft/delete/{userId}")
    @Operation(summary = "회원 소프트 삭제", description = "회원을 논리적으로 삭제합니다 (복구 가능).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "소프트 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<User> softDeleteUser(
        @Parameter(description = "삭제할 회원 ID", required = true, example = "1")
        @PathVariable Long userId) {
        validateUserId(userId);
        User deletedUser = adminService.changeUserState(userId, State.INACTIVE);
        return new BaseResponse<>(deletedUser);
    }

    @DeleteMapping("/users/hard/delete/{userId}")
    @Operation(summary = "회원 하드 삭제", description = "회원을 물리적으로 삭제합니다 (복구 불가능).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "하드 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<BaseResponseStatus> hardDeleteUser(
        @Parameter(description = "삭제할 회원 ID", required = true, example = "1")
        @PathVariable Long userId) {
        validateUserId(userId);
        adminService.deleteUser(userId);
        return new BaseResponse<>(BaseResponseStatus.NO_CONTENT);
    }

    @PatchMapping("/user/{userId}")
    @Operation(summary = "관리자용 회원 정보 수정", description = "관리자가 회원 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public BaseResponse<User> updateUserInfo(
        @Parameter(description = "수정할 회원 ID", required = true, example = "1")
        @PathVariable Long userId,
        @Valid @RequestBody UpdateUserReq updateUserReq) {
        validateUserId(userId);
        User updatedUser = adminService.updateUser(userId, updateUserReq);
        return new BaseResponse<>(updatedUser);
    }

    private static void validateUserId(Long userId) {
        checkUserIdNull(userId);
        checkUserIdNegative(userId);
    }


    private static void validateParameters(int page, int size) {
        if (page < 0) {
            throw new BaseException(BaseResponseStatus.INVALID_PAGE);
        }
        if (size <= 0 || size > 100) {
            throw new BaseException(BaseResponseStatus.INVALID_PAGE_SIZE);
        }
    }

    private static void checkUserIdNull(Long userId) {
        if (userId == null) {
            throw new BaseException(BaseResponseStatus.INVALID_ID);
        }
    }

    private static void checkUserIdNegative(Long userId) {
        if (userId <= 0) {
            throw new BaseException(BaseResponseStatus.INVALID_ID);
        }
    }

    private static void validateDate(LocalDateTime date) {
        if (date == null) {
            return;
        }
        ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE);

        if (date.isAfter(now)) {
            log.warn("미래 날짜로 가입 날짜 검색 시도 {}", date);
            throw new BaseException(BaseResponseStatus.INVALID_DATE);
        }
    }
}
