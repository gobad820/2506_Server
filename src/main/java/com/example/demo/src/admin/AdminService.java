package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import java.time.LocalDate;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminDataManager adminDataManager;
    private final JwtService jwtService;


    public User getUserById(Long id) {
        User user = adminDataManager.getUserInfoById(id)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        return user;
    }

    public Page<User> getUsersInfo(int page, int size, String name, Long id, LocalDate createdDate,
        State state) {
        Specification<User> spec = buildUserSpecification(name, id, createdDate, state);
        Pageable pageable = getPageable(page, size);
        return adminDataManager.getAllUserInfos(spec, pageable);
    }

    public User changeUserState(Long userId, State state) {
        User foundUser = adminDataManager.getUserInfoById(userId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        foundUser.updateState(state);
        return adminDataManager.saveUser(foundUser);
    }

    private static Specification<User> buildUserSpecification(
        String name,
        Long id,
        LocalDate createdDate,
        State state) {
        return Specification.where(UserSpecifications.containsName(name))
            .and(UserSpecifications.hasId(id))
            .and(UserSpecifications.createdOnDate(createdDate))
            .and(UserSpecifications.hasState(state));
    }

    private static @NotNull PageRequest getPageable(int page, int size) {
        return PageRequest.of(page, size);
    }
}
