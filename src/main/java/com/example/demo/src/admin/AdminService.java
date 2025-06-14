package com.example.demo.src.admin;

import static com.example.demo.common.Constant.KOREA_ZONE;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.admin.model.UpdateUserReq;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminDataManager adminDataManager;
    private final JwtService jwtService;


    public User getUserById(Long userId) {
        User user = adminDataManager.getUserInfoById(userId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        return user;
    }

    public Page<User> getUsersInfo(int page, int size, String name, Long id,
        LocalDateTime createdDate,
        State state) {
        Specification<User> spec = buildUserSpecification(name, id, createdDate, state);
        Pageable pageable = getPageable(page, size);
        return adminDataManager.getAllUserInfos(spec, pageable);
    }

    @Transactional
    public User changeUserState(Long userId, State state) {
        validateUserState(getUserById(userId).getState());
        User foundUser = adminDataManager.getUserInfoById(userId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        foundUser.updateState(state);
        return adminDataManager.saveUser(foundUser);
    }

    private static Specification<User> buildUserSpecification(
        String name,
        Long id,
        LocalDateTime createdDate,
        State state) {
        return Specification.where(UserSpecifications.containsName(name))
            .and(UserSpecifications.hasId(id))
            .and(UserSpecifications.createdOnDate(createdDate))
            .and(UserSpecifications.hasState(state));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User foundUser = getUserById(userId);
        validateForHardDeletion(foundUser);
        try {
            adminDataManager.deleteUser(foundUser.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("데이터 무결성 제약 조건 위반 {}", userId);
            throw new BaseException(BaseResponseStatus.DELETE_FAIL_USERID);
        }
    }

    private static void validateForHardDeletion(User foundUser) {
        if (foundUser.getState() != State.INACTIVE) {
            throw new BaseException(BaseResponseStatus.NOT_SOFT_DELETED_USER);
        }
        if (foundUser.getUpdatedAt() != null && foundUser.getUpdatedAt()
            .isAfter(LocalDateTime.now(KOREA_ZONE).minusDays(7))) {
            throw new BaseException(BaseResponseStatus.TOO_SOON_TO_DELETE);
        }
    }

    private static @NotNull PageRequest getPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    private static void validateUserState(State state) {
        if (state == State.INACTIVE) {
            throw new BaseException(BaseResponseStatus.DELETED_USER);
        }
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserReq updateUserReq) {
        validateUserState(updateUserReq.getState());
        User foundUser = adminDataManager.getUserByIdAndState(userId, State.ACTIVE)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        foundUser.updateFields(updateUserReq);
        return foundUser;
    }
}
