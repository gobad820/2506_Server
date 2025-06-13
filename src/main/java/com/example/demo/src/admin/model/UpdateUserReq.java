package com.example.demo.src.admin.model;

import com.example.demo.common.entity.BaseEntity.State;
import javax.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserReq {


    private String name;
    @Email
    private String email;
    private State state;

    public boolean hasName() {
        return this.name != null;
    }

    public boolean hasEmail() {
        return this.email != null;
    }

    public boolean hasState() {
        return this.state != null;
    }

}
