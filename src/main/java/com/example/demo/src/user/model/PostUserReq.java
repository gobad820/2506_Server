package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "회원가입 요청")
public class PostUserReq {

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String email;

    @Schema(description = "비밀번호", example = "password123!", requiredMode = RequiredMode.REQUIRED)
    @ToString.Exclude
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동",requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "이름은 2-20자 이상이어야 합니다.")
    private String name;

    private boolean isOAuth;

    public User toEntity() {
        return User.builder()
            .email(this.email)
            .password(this.password)
            .name(this.name)
            .isOAuth(this.isOAuth)
            .build();
    }
}
