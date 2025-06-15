package com.example.demo.src.audit;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.audit.model.UserAuditRes;
import com.example.demo.src.audit.model.UserAuditReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@Tag(name = "감사 로그", description = "시스템 감사 로그 조회 API")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/app/audit")
public class AuditController {

    private final AuditService auditService;

    /**
     * 특정 사용자 감사 기록 조회 API
     * <p>
     * [GET] /app/audit/user/{userId}
     * </p>
     *
     * @param userId 조회할 사용자 Id
     * @return 해당 사용자의 모든 기록 로그
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "특정 사용자 감사 기록 조회", description = "특정 사용자의 모든 변경 이력을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")})
    public BaseResponse<List<UserAuditRes>> getUserAudit(
        @Parameter(description = "조회할 사용자 ID", required = true, example = "1") @PathVariable @Min(value = 1, message = "사용자 ID는 1 이상이어야 합니다.") Long userId) {
        List<UserAuditRes> userAudit = auditService.getUserAudit(userId);
        return new BaseResponse<>(userAudit);
    }


    /**
     * 시스템 전체 감사 기록 조회 API - 페이지네이션
     * <p>[GET] /app/audit/system </p>
     *
     * @param request
     * @param pageable
     * @return
     */
    @GetMapping("/system")
    @Operation(summary = "시스템 전체 감사 기록 조회", description = "시스템 전체의 감사 기록을 페이지네이션으로 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터 (BaseResponseStatus.INVALID_REQUEST_PARAM)"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (BaseResponseStatus.INVALID_JWT)"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요 (BaseResponseStatus.INVALID_USER_JWT)")})
    public BaseResponse<Page<UserAuditRes>> getSystemAuditHistory(
        @ModelAttribute @Valid UserAuditReq request,
        @Parameter(description = "페이지 정보 (page, size, sort)") @PageableDefault(size = 10) Pageable pageable) {
        log.info(request.toString());
        Page<UserAuditRes> auditHistory = auditService.getSystemAuditHistory(request, pageable);
        return new BaseResponse<>(auditHistory);
    }


    /**
     * 특정 리비전의 상세 정보 조회 API
     * <p>[GET] /api/audit/revision/{revisionId}</p>
     *
     * @param revisionId
     * @return
     */
    @GetMapping("/revisions/{revisionId}")
    @Operation(summary = "특정 리비전 상세 정보 조회", description = "특정 리비전의 상세 변경 내용을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "리비전 ID가 너무 큼 (BaseResponseStatus.REVISION_ID_TOO_LARGE)"),
        @ApiResponse(responseCode = "404", description = "리비전을 찾을 수 없음 (BaseResponseStatus.AUDIT_DATA_NOT_FOUND)"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (BaseResponseStatus.INVALID_JWT)"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요 (BaseResponseStatus.INVALID_USER_JWT)")})
    public BaseResponse<UserAuditRes> getRevisionDetail(
        @Parameter(description = "리비전 ID", required = true, example = "1")
        @PathVariable @Min(value = 1, message = "리비전 ID는 1 이상이어야 합니다.")
        @Max(value = Integer.MAX_VALUE, message = "리비전 ID 값이 너무 큽니다.")
        Long revisionId) {
        log.info("revision ID {}의 revision detail을 조회", revisionId);
        log.info("리비전 디테일 조회, revisionId: {}", revisionId);
        UserAuditRes revisionDetail = auditService.getRevisionDetail(revisionId);
        return new BaseResponse<>(revisionDetail);
    }
}
