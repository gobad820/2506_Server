package com.example.demo.src.audit.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.RevisionType;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserAuditReq {

    @Min(value = 1, message = "사용자 ID는 1 이상이어야 합니다.")
    private Long targetUserId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDateTime;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDateTime;

    private RevisionType revisionType;

    @AssertTrue(message = "시작 날짜가 종료 날짜보다 미래일 수는 없습니다.")
    public boolean validateDateRange() {
        if (startDateTime == null || endDateTime == null) {
            return true;
        }
        return !startDateTime.isAfter(endDateTime);
    }

}
