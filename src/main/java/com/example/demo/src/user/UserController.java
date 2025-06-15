package com.example.demo.src.user;


import static com.example.demo.common.response.BaseResponseStatus.INVALID_USER_JWT;

import com.example.demo.common.Constant.SocialLoginType;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.oauth.OAuthService;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.user.model.GetSocialOAuthRes;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.src.user.model.PatchUserReq;
import com.example.demo.src.user.model.PostLoginReq;
import com.example.demo.src.user.model.PostLoginRes;
import com.example.demo.src.user.model.PostUserReq;
import com.example.demo.src.user.model.PostUserRes;
import com.example.demo.src.user.model.UserConsentReq;
import com.example.demo.utils.JwtService;
import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/app/users")
public class UserController {


    private final UserService userService;

    private final OAuthService oAuthService;

    private final JwtService jwtService;


    /**
     * 회원가입 API [POST] /app/users
     *
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@Valid @RequestBody PostUserReq postUserReq) {
        PostUserRes postUserRes = userService.createUser(postUserReq);
        return new BaseResponse<>(postUserRes);
    }

    /**
     * 회원 조회 API [GET] /users 회원 번호 및 이메일 검색 조회 API [GET] /app/users? Email=
     *
     * @return BaseResponse<List < GetUserRes>>
     */
    // Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    public BaseResponse<List<GetUserRes>> getUsers(
        @RequestParam(required = false)
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "이메일 형식이 올바르지 않습니다.")
        String Email
    ) {
        if (Email == null) {
            List<GetUserRes> getUsersRes = userService.getUsers();
            return new BaseResponse<>(getUsersRes);
        }
        List<GetUserRes> getUsersRes = userService.getUsersByEmail(Email);
        return new BaseResponse<>(getUsersRes);
    }

    /**
     * 회원 1명 조회 API [GET] /app/users/:userId
     *
     * @return BaseResponse<GetUserRes>
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userId}") // (GET) 127.0.0.1:9000/app/users/:userId
    public BaseResponse<GetUserRes> getUser(
        @PathVariable("userId")
        @Min(value = 1, message = "사용자 ID는 1 이상이어야 합니다.")
        Long userId) {
        GetUserRes getUserRes = userService.getUser(userId);
        return new BaseResponse<>(getUserRes);
    }


    /**
     * 유저정보변경 API [PATCH] /app/users/:userId
     *
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}")
    public BaseResponse<String> modifyUserName(
        @PathVariable("userId")
        @Min(value = 1, message = "사용자 ID는 1 이상이어야 합니다.")
        Long userId,
        @Valid @RequestBody PatchUserReq patchUserReq) {

        Long jwtUserId = jwtService.getUserId();
        if (!userId.equals(jwtUserId)) {
            throw new BaseException(INVALID_USER_JWT);
        }

        userService.modifyUserName(userId, patchUserReq);
        String result = "수정 완료!!";
        return new BaseResponse<>(result);

    }

    /**
     * 유저정보삭제 API [DELETE] /app/users/:userId
     *
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/{userId}")
    public BaseResponse<String> deleteUser(@PathVariable("userId") Long userId) {
        log.info("=======UserID========\n{}", userId);
        Long jwtUserId = jwtService.getUserId();

        if (!userId.equals(jwtUserId)) {
            throw new BaseException(INVALID_USER_JWT);
        }

        userService.deleteUser(userId);
        String result = "삭제 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 로그인 API [POST] /app/users/logIn
     *
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/logIn")
    public BaseResponse<PostLoginRes> logIn(
        @Valid @RequestBody PostLoginReq postLoginReq) {
        log.info("=======PostLoginReq=======\n{}", postLoginReq.toString());
        PostLoginRes postLoginRes = userService.logIn(postLoginReq);
        return new BaseResponse<>(postLoginRes);
    }


    /**
     * 유저 소셜 가입, 로그인 인증으로 리다이렉트 해주는 url [GET] /app/users/auth/:socialLoginType/login
     *
     * @return void
     */
    @GetMapping("/auth/{socialLoginType}/login")
    public void socialLoginRedirect(
        @PathVariable(name = "socialLoginType")
        @Pattern(regexp = "^(GOOGLE|NAVER|KAKAO)$",
            message = "지원하지 않는 소셜 로그인 타입입니다.")
        String SocialLoginPath)
        throws IOException {
        SocialLoginType socialLoginType = SocialLoginType.valueOf(SocialLoginPath.toUpperCase());
        oAuthService.accessRequest(socialLoginType);
    }


    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     *
     * @param socialLoginPath (GOOGLE, FACEBOOK, NAVER, KAKAO)
     * @param code            API Server 로부터 넘어오는 code
     * @return SNS Login 요청 결과로 받은 Json 형태의 java 객체 (access_token, jwt_token, user_num 등)
     */
    @ResponseBody
    @GetMapping(value = "/auth/{socialLoginType}/login/callback")
    public BaseResponse<GetSocialOAuthRes> socialLoginCallback(
        @PathVariable(name = "socialLoginType")
        @Pattern(regexp = "^(GOOGLE|NAVER|KAKAO)$"
            , message = "지원하지 않는 소셜 로그인 타입입니다.")
        String socialLoginPath,
        @RequestParam(name = "code")
        @NotBlank(message = "인증 코드가 필요합니다.")
        String code) throws IOException, BaseException {
        log.info(">> 소셜 로그인 API 서버로부터 받은 code : {}", code);
        SocialLoginType socialLoginType = SocialLoginType.valueOf(socialLoginPath.toUpperCase());
        GetSocialOAuthRes getSocialOAuthRes = oAuthService.oAuthLoginOrJoin(socialLoginType, code);
        return new BaseResponse<>(getSocialOAuthRes);
    }

    @ResponseBody
    @PostMapping("/consent")
    public BaseResponse<String> updateUserConsent(
        @Valid @RequestBody UserConsentReq consentReq
    ) {
        Long jwtUserId = jwtService.getUserId();
        log.info("개인정보 동의 : userId = {}", jwtUserId);
        jwtService.updateUserConsent(jwtUserId, consentReq);
        return new BaseResponse<>("개인정보 동의 설정이 변경되었습니다.");
    }

    @ResponseBody
    @PostMapping("/unlock")
    public BaseResponse<String> requestAccountUnlock(
        @RequestParam
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수값입니다.")
        String email
    ) {
        log.info("계정 잠금 해제 요청: email = {}", email);
        userService.requestAccountUnlock(email);

        return new BaseResponse<>("계정 잠금 해제 요청이 처리되었습니다. 관리자 승인 후 해제됩니다.");
    }
}
