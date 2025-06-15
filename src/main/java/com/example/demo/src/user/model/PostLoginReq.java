package com.example.demo.src.user.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostLoginReq {

    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @ToString.Exclude
    private String password;
}
