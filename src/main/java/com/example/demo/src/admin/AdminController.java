package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.user.entity.User;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/users/{userId}")
    public BaseResponse<User> getSingleUserInfoForAdmin(@PathVariable Long userId) {
        checkUserIdNull(userId);
        User userById = adminService.getUserById(userId);
        return new BaseResponse<>(userById);
    }

    @GetMapping("/users")
    public BaseResponse<Page<User>> getUsersInfoForAdmin(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String name,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
        @RequestParam(required = false) State state) {
        validateParameters(page, size);
        if (userId != null) {
            checkUserIdNegative(userId);
        }
        validateDate(createdDate);
        Page<User> users = adminService.getUsersInfo(page, size, name, userId, createdDate, state);
        return new BaseResponse<>(users);
    }

    @PostMapping("/users/inactivate/{userId}")
    public BaseResponse<User> inActivateUser(@PathVariable Long userId) {
        checkUserIdNull(userId);
        checkUserIdNegative(userId);
        User user = adminService.changeUserState(userId, State.INACTIVE);
        return new BaseResponse<>(user);
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

    private static void validateDate(LocalDate date) {
        if (date == null) {
            return;
        }
        ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(KOREA_ZONE);

        if (date.isAfter(now)) {
            log.warn("미래 날짜로 가입 날짜 검색 시도 {}", date);
            throw new BaseException(BaseResponseStatus.INVALID_DATE);
        }

    }
}
