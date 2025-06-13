package com.example.demo.src.audit.dto;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.src.user.entity.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.RevisionType;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAuditDto {

    private Long revision;
    private LocalDateTime revisionDateTime;
    private RevisionType revisionType;
    private Long userId;
    private String userName;
    private String userEmail;
    private State userState;

    public static UserAuditDto from(User user, Long revision, LocalDateTime revisionDateTime,
        RevisionType revisionType) {
        return UserAuditDto.builder()
            .revision(revision)
            .revisionDateTime(revisionDateTime)
            .revisionType(revisionType)
            .userId(user.getId())
            .userName(user.getName())
            .userEmail(user.getEmail())
            .userState(user.getState())
            .build();
    }

}
