package com.example.demo.src.audit;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.audit.dto.UserAuditDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/audit")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/users/{userId}")
    public BaseResponse<List<UserAuditDto>> getUserAudit(@PathVariable Long userId) {
        List<UserAuditDto> userAudit = auditService.getUserAudit(userId);
        return new BaseResponse<>(userAudit);
    }


}
