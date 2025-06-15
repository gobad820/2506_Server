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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "회원 관리", description = "회원 가입, 로그인, 정보 수정 등 회원 API")
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
    @ResponseBody
    @PostMapping("")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    })
    public BaseResponse<PostUserRes> createUser(@Valid @RequestBody PostUserReq postUserReq) {
        PostUserRes postUserRes = userService.createUser(postUserReq);
        return new BaseResponse<>(postUserRes);
    }

    /**
     * 회원 조회 API [GET] /users 회원 번호 및 이메일 검색 조회 API [GET] /app/users? email=
     *
     * @return BaseResponse<List < GetUserRes>>
     */
    @ResponseBody
    @GetMapping("")
    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회하거나 이메일로 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식")
    })
    public BaseResponse<List<GetUserRes>> getUsers(
        @Parameter(description = "검색할 이메일 주소 (선택사항)")
        @RequestParam(required = false)
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "이메일 형식이 올바르지 않습니다.")
        String email) {
        if (email == null) {
            List<GetUserRes> getUsersRes = userService.getUsers();
            return new BaseResponse<>(getUsersRes);
        }
        List<GetUserRes> getUsersRes = userService.getUsersByEmail(email);
        return new BaseResponse<>(getUsersRes);
    }

    /**
     * 회원 1명 조회 API [GET] /app/users/:userId
     *
     * @return BaseResponse<GetUserRes>
     */
    @ResponseBody
    @GetMapping("/{userId}")
    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<GetUserRes> getUser(
        @Parameter(description = "회원 ID", required = true, example = "1")
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
    @Operation(summary = "회원 정보 수정", description = "회원의 정보를 수정합니다. 본인만 수정 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인이 아님)")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> modifyUserName(
        @Parameter(description = "회원 ID", required = true, example = "1")
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
    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴 처리합니다. 본인만 탈퇴 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인이 아님)")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> deleteUser(
        @Parameter(description = "회원 ID", required = true, example = "1")
        @PathVariable("userId")
        @Min(value = 1, message = "사용자 ID는 1 이상이어야 합니다.")
        Long userId) {
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
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 로그인 정보"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public BaseResponse<PostLoginRes> logIn(@Valid @RequestBody PostLoginReq postLoginReq) {
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
    @Operation(summary = "소셜 로그인 리다이렉트", description = "소셜 로그인 인증 페이지로 리다이렉트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "리다이렉트 성공"),
        @ApiResponse(responseCode = "400", description = "지원하지 않는 소셜 로그인 타입")
    })
    public void socialLoginRedirect(
        @Parameter(description = "소셜 로그인 타입", example = "GOOGLE")
        @PathVariable(name = "socialLoginType")
        @Pattern(regexp = "^(GOOGLE|NAVER|KAKAO)$", message = "지원하지 않는 소셜 로그인 타입입니다.")
        String SocialLoginPath) throws IOException {
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
    @Operation(summary = "소셜 로그인 콜백", description = "소셜 로그인 인증 서버로부터의 콜백을 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "소셜 로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
    })
    public BaseResponse<GetSocialOAuthRes> socialLoginCallback(
        @Parameter(description = "소셜 로그인 타입", example = "GOOGLE")
        @PathVariable(name = "socialLoginType")
        @Pattern(regexp = "^(GOOGLE|NAVER|KAKAO)$", message = "지원하지 않는 소셜 로그인 타입입니다.")
        String socialLoginPath,
        @Parameter(description = "소셜 로그인 인증 코드", required = true)
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
    @Operation(summary = "개인정보 동의 설정", description = "사용자의 개인정보 동의 설정을 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동의 설정 변경 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> updateUserConsent(@Valid @RequestBody UserConsentReq consentReq) {
        Long jwtUserId = jwtService.getUserId();
        log.info("개인정보 동의 : userId = {}", jwtUserId);
        jwtService.updateUserConsent(jwtUserId, consentReq);
        return new BaseResponse<>("개인정보 동의 설정이 변경되었습니다.");
    }


    @ResponseBody
    @PostMapping("/unlock")
    @Operation(summary = "계정 잠금 해제 요청", description = "잠긴 계정의 해제를 요청합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "잠금 해제 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public BaseResponse<String> requestAccountUnlock(
        @Parameter(description = "잠금 해제할 계정의 이메일", required = true, example = "user@example.com")
        @RequestParam
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수값입니다.")
        String email) {
        log.info("계정 잠금 해제 요청: email = {}", email);
        userService.requestAccountUnlock(email);
        return new BaseResponse<>("계정 잠금 해제 요청이 처리되었습니다. 관리자 승인 후 해제됩니다.");
    }
}
