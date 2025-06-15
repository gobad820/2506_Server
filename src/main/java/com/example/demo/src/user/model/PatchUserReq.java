package com.example.demo.src.user.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchUserReq {
    @NotBlank(message = "이름은 필수값입니다.")
    @Size(min=2,max=20,message = "이름은 2-20자 이상이어야 합니다.")
    private String name;
}
