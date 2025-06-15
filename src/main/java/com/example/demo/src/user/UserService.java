package com.example.demo.src.user;


import static com.example.demo.common.Constant.WITHDRAWL_GRACE_PERIOD_DAYS;
import static com.example.demo.common.entity.BaseEntity.State.ACTIVE;
import static com.example.demo.common.entity.BaseEntity.State.LOCKED;
import static com.example.demo.common.response.BaseResponseStatus.ACCOUNT_NOT_LOCKED;
import static com.example.demo.common.response.BaseResponseStatus.DATABASE_ERROR;
import static com.example.demo.common.response.BaseResponseStatus.DELETED_USER;
import static com.example.demo.common.response.BaseResponseStatus.FAILED_TO_LOGIN;
import static com.example.demo.common.response.BaseResponseStatus.INVALID_REQUEST_PARAM;
import static com.example.demo.common.response.BaseResponseStatus.LOCKED_USER;
import static com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER;
import static com.example.demo.common.response.BaseResponseStatus.PASSWORD_ENCRYPTION_ERROR;
import static com.example.demo.common.response.BaseResponseStatus.POST_USERS_EXISTS_EMAIL;
import static com.example.demo.common.response.BaseResponseStatus.USERNAME_UNCHANGED;
import static com.example.demo.common.response.BaseResponseStatus.USERS_EMPTY_EMAIL;
import static com.example.demo.common.response.BaseResponseStatus.WITHDRAWN_USER;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.user.entity.User;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.src.user.model.PatchUserReq;
import com.example.demo.src.user.model.PostLoginReq;
import com.example.demo.src.user.model.PostLoginRes;
import com.example.demo.src.user.model.PostUserReq;
import com.example.demo.src.user.model.PostUserRes;
import com.example.demo.src.user.model.UserConsentReq;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service Create, Update, Delete 의 로직 처리
@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;



    // POST
    public PostUserRes createUser(PostUserReq postUserReq) {
        log.info("회원 가입: email = {}", postUserReq.getEmail());
        // 중복 체크
        Optional<User> checkUser = userRepository.findUserByEmailAndState(postUserReq.getEmail(),
            ACTIVE);
        if (checkUser.isPresent()) {
            log.warn("이메일 중복: email = {}", postUserReq.getEmail());
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        String encryptPwd;
        try {
            encryptPwd = SHA256.encrypt(postUserReq.getPassword());
            postUserReq.setPassword(encryptPwd);
        } catch (Exception exception) {
            log.error("비밀번호 암호화 실패: email = {}", postUserReq.getEmail());
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try {
            User saveUser = userRepository.save(postUserReq.toEntity());
            log.info("회원가입 성공: userId = {}, email = {}", saveUser.getId(), saveUser.getEmail());
            return new PostUserRes(saveUser.getId());
        } catch (Exception e) {
            log.error("데이터베이스 저장 실패: email = {}", postUserReq.getEmail(), e);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    public PostUserRes createOAuthUser(User user) {
        if (user == null) {
            log.error("OAuth 정보가 없습니다.");
            throw new BaseException(INVALID_REQUEST_PARAM);
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("OAuth 사용자 이메일이 존재하지 않습니다.");
            throw new BaseException(USERS_EMPTY_EMAIL);
        }

        log.info("OAuth 사용자 생성 로그인 시도: email ={}", user.getEmail());

        Optional<User> userByIdAndState = userRepository.findUserByIdAndState(user.getId(), ACTIVE);
        if (userByIdAndState.isPresent()) {
            log.info("기존 사용자 로그인 email ={}", user.getEmail());
            try {
                String jwt = jwtService.createJwt(userByIdAndState.get().getId());
                return new PostUserRes(userByIdAndState.get().getId(), jwt);
            } catch (Exception e) {
                log.error("JWT 생성 실패: userId={}", userByIdAndState.get().getId(), e);
                throw new BaseException(BaseResponseStatus.JWT_CREATE_ERROR);
            }
        }

        try {
            User saveUser = userRepository.save(user);
            String jwtToken = jwtService.createJwt(saveUser.getId());
            log.info("OAuth 사용자 생성 성공: userId = {}, email = {}", saveUser.getId(),
                saveUser.getEmail());
            return new PostUserRes(saveUser.getId(), jwtToken);
        } catch (Exception e) {
            log.error("OAuth 사용자 저장 실패 email = {}", user.getEmail());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void modifyUserName(Long userId, PatchUserReq patchUserReq) {
        log.info("유저명 수정: userId = {}, userName = {}", userId, patchUserReq.getName());

        User user = userRepository.findUserByIdAndState(userId, ACTIVE)
            .orElseThrow(() -> {
                log.warn("존재하지 않는 유저입니다. userId = {}", userId);
                return new BaseException(NOT_FIND_USER);
            });

        if (user.getName().equals(patchUserReq.getName())) {
            log.warn("변경 전 유저명과 동일합니다.");
            throw new BaseException(USERNAME_UNCHANGED);
        }

        try {
            user.updateName(patchUserReq.getName());
            log.info("사용자 이름 수정 성공: userId = {}, newName = {}", userId, patchUserReq.getName());
        } catch (Exception e) {
            log.error("데이터베이스 업데이트 실패: userId = {}", userId, e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findUserByIdAndState(userId, ACTIVE)
            .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.deleteUser();
    }

    @Transactional(readOnly = true)
    public List<GetUserRes> getUsers() {
        List<GetUserRes> getUserResList = userRepository.findAllByState(ACTIVE).stream()
            .map(GetUserRes::new)
            .collect(Collectors.toList());
        return getUserResList;
    }

    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByEmail(String email) {
        List<GetUserRes> getUserResList = userRepository.findAllByEmailAndState(email, ACTIVE)
            .stream()
            .map(GetUserRes::new)
            .collect(Collectors.toList());
        return getUserResList;
    }


    @Transactional(readOnly = true)
    public GetUserRes getUser(Long userId) {
        User user = userRepository.findUserByIdAndState(userId, ACTIVE)
            .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return new GetUserRes(user);
    }

    @Transactional(readOnly = true)
    public boolean checkUserByEmail(String email) {
        Optional<User> result = userRepository.findUserByEmailAndState(email, ACTIVE);
        if (result.isPresent()) {
            return true;
        }
        return false;
    }

    public PostLoginRes logIn(PostLoginReq postLoginReq) {
        User user = userRepository.findUserByEmail(postLoginReq.getEmail())
            .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        validateUserState(user);

        String encryptPwd;
        try {
            encryptPwd = SHA256.encrypt(postLoginReq.getPassword());
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        if (user.getPassword().equals(encryptPwd)) {
            Long userId = user.getId();
            String jwt = jwtService.createJwt(userId);
            return new PostLoginRes(userId, jwt);
        } else {
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    private static void validateUserState(User user) {
        if (user.getState() == State.INACTIVE) {
            ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
            boolean isPastGracePeriod = user.getUpdatedAt().plusDays(WITHDRAWL_GRACE_PERIOD_DAYS)
                .isBefore(LocalDateTime.now(KOREA_ZONE));
            if (isPastGracePeriod) {
                throw new BaseException(WITHDRAWN_USER);
            } else {
                throw new BaseException(DELETED_USER);
            }
        } else if (user.getState() == LOCKED) {
            throw new BaseException(LOCKED_USER);
        }
    }

    public GetUserRes getUserByEmail(String email) {
        User user = userRepository.findUserByEmailAndState(email, ACTIVE)
            .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return new GetUserRes(user);
    }

    public void requestAccountUnlock(
        @Email(message = "유효한 이메일 형식이어야 합니다.") @NotBlank(message = "이메일은 필수값입니다.") String email) {
        User user = userRepository.findUserByEmail(email)
            .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        if (user.getState() != LOCKED) {
            throw new BaseException(ACCOUNT_NOT_LOCKED);
        }

        try {
            user.updateState(ACTIVE);
            userRepository.save(user);
            log.info("계정 잠금 해제: email = {}", email);
        } catch (Exception e) {
            log.error("계정 잠금 해제 실패: email = {}, error = {}", email, e.getMessage(), e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
