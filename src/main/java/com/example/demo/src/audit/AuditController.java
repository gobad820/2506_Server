package com.example.demo.src.audit;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.audit.dto.UserAuditDto;
import com.example.demo.src.audit.dto.UserAuditReq;
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
    public BaseResponse<List<UserAuditDto>> getUserAudit(
        @PathVariable @Min(value = 1, message = "사용자 ID는 1 이사잉어야 합니다.") Long userId) {
        List<UserAuditDto> userAudit = auditService.getUserAudit(userId);
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
    public BaseResponse<Page<UserAuditDto>> getSystemAuditHistory(
        @ModelAttribute @Valid UserAuditReq request,
        @PageableDefault(size = 10) Pageable pageable) {
        log.info(request.toString());
        Page<UserAuditDto> auditHistory = auditService.getSystemAuditHistory(request,
            pageable);
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
    public BaseResponse<UserAuditDto> getRevisionDetail(
        @PathVariable @Min(value = 1, message = "리비전 ID는 1 이상이어야 합니다.") @Max(value = Integer.MAX_VALUE, message = "리비전 ID 값이 너무 큽니다.") Long revisionId) {
        log.info("revision ID {}의 reivision detail을 조회", revisionId);
        log.info("리비전 디테일 조회, revisionId: {}", revisionId);
        UserAuditDto revisionDetail = auditService.getRevisionDetail(revisionId);
        return new BaseResponse<>(revisionDetail);
    }
}
