package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.user.entity.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/admin")
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/users")
    public BaseResponse<Page<User>> getUsersInfoForAdmin(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String name,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime createdDate,
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
    public BaseResponse<User> getSingleUserInfoForAdmin(@PathVariable Long userId) {
        validateUserId(userId);
        User userById = adminService.getUserById(userId);
        return new BaseResponse<>(userById);
    }

    @PostMapping("/users/inactivate/{userId}")
    public BaseResponse<User> inActivateUser(@PathVariable Long userId) {
        validateUserId(userId);
        User user = adminService.changeUserState(userId, State.INACTIVE);
        return new BaseResponse<>(user);
    }

    @PatchMapping("/users/soft/delete/{userId}")
    public BaseResponse<User> softDeleteUser(@PathVariable Long userId) {
        validateUserId(userId);
        User deletedUser = adminService.changeUserState(userId, State.DELETED);
        return new BaseResponse<>(deletedUser);
    }

    @DeleteMapping("/users/hard/delete/{userId}")
    public BaseResponse<User> hardDeleteUser(@PathVariable Long userId) {
        validateUserId(userId);
        User deletedUser = adminService.deleteUser(userId);
        return new BaseResponse<>(deletedUser);
    }

    @PatchMapping("/user/update/{userId}")
    public BaseResponse<User> updateUserInfo(@PathVariable Long userId,
        @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String name,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime createdDate,
        @RequestParam(required = false) State state) {
        validateUserId(userId);
        validateDate(createdDate);
        adminService.updateUser(userId, name, createdDate, state);
        return null;
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
