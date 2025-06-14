package com.example.demo.src.test.model;

import com.example.demo.src.test.entity.Memo;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoDto {

    @NotBlank(message = "메모를 입력해주세요.")
    private String memo;

    public Memo toEntity() {
        return Memo.builder()
                .memo(this.memo)
                .build();
    }

}
